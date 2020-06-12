/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 OnyxStudios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dev.onyxstudios.cca.internal.base.asm;

import dev.onyxstudios.cca.api.v3.component.StaticComponentInitializer;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import nerdhub.cardinal.components.api.ComponentType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.Identifier;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.BiConsumer;

public final class CcaBootstrap extends LazyDispatcher {

    public static final String COMPONENT_TYPE_INIT_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(CcaAsmHelper.IDENTIFIER), Type.getType(Class.class), Type.INT_TYPE);
    public static final String COMPONENT_TYPE_GET0_DESC = "(L" + CcaAsmHelper.COMPONENT_PROVIDER + ";)L" + CcaAsmHelper.COMPONENT + ";";
    public static final String STATIC_INIT_ENTRYPOINT = "cardinal-components:static-init";
    public static final CcaBootstrap INSTANCE = new CcaBootstrap();

    private final List<EntrypointContainer<StaticComponentInitializer>> staticComponentInitializers = FabricLoader.getInstance().getEntrypointContainers(STATIC_INIT_ENTRYPOINT, StaticComponentInitializer.class);

    private Map<Identifier, Class<? extends ComponentType<?>>> generatedComponentTypes = new HashMap<>();

    public CcaBootstrap() {
        super("registering a ComponentType");
    }

    @Nullable
    public Class<? extends ComponentType<?>> getGeneratedComponentTypeClass(Identifier componentId) {
        this.ensureInitialized();
        assert this.generatedComponentTypes != null;
        return this.generatedComponentTypes.get(componentId);
    }

    public <T extends StaticComponentInitializer> void processSpecializedInitializers(Class<T> initializerType, BiConsumer<T, ModContainer> action) {
        this.ensureInitialized();

        for (EntrypointContainer<StaticComponentInitializer> staticInitializer : this.staticComponentInitializers) {
            if (initializerType.isInstance(staticInitializer.getEntrypoint())) {
                @SuppressWarnings("unchecked") EntrypointContainer<T> t = (EntrypointContainer<T>) staticInitializer;
                try {
                    action.accept(t.getEntrypoint(), staticInitializer.getProvider());
                } catch (Throwable e) {
                    ModMetadata metadata = staticInitializer.getProvider().getMetadata();
                    throw new StaticComponentLoadingException(String.format("Exception while registering static component factories for %s (%s)", metadata.getName(), metadata.getId()), e);
                }
            }
        }
    }

    @Override
    protected void init() {
        try {
            Set<Identifier> staticComponentTypes = new HashSet<>();

            for (EntrypointContainer<StaticComponentInitializer> staticInitializer : this.staticComponentInitializers) {
                try {
                    staticComponentTypes.addAll(staticInitializer.getEntrypoint().getSupportedComponentTypes());
                } catch (Throwable e) {
                    ModMetadata badMod = staticInitializer.getProvider().getMetadata();
                    throw new StaticComponentLoadingException(String.format("Exception while querying %s (%s) for supported static component types", badMod.getName(), badMod.getId()), e);
                }
            }

            this.generatedComponentTypes = this.spinStaticComponentTypes(staticComponentTypes);
        } catch (IOException | UncheckedIOException e) {
            throw new StaticComponentLoadingException("Failed to load statically defined components", e);
        }
    }

    @Override
    protected void postInit() {
        for (EntrypointContainer<StaticComponentInitializer> staticInitializer : this.staticComponentInitializers) {
            staticInitializer.getEntrypoint().finalizeStaticBootstrap();
        }
    }

    /**
     * Defines a {@link ComponentType} subclass for every statically declared component, as well as
     * a global {@link nerdhub.cardinal.components.api.component.ComponentProvider} specialized interface
     * that declares a direct getter for every {@link ComponentType} that has been scanned by plugins.
     *
     * @param staticComponentTypes the set of all statically declared {@link ComponentType} ids
     * @return a map of {@link ComponentType} ids to specialized implementations
     */
    private Map<Identifier, Class<? extends ComponentType<?>>> spinStaticComponentTypes(Set<Identifier> staticComponentTypes) throws IOException {
        /* generate the component container getter */

        ClassNode staticContainerWriter = new ClassNode(CcaAsmHelper.ASM_VERSION);
        staticContainerWriter.visit(Opcodes.V1_8, Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE, CcaAsmHelper.STATIC_COMPONENT_CONTAINER, null, "java/lang/Object", new String[]{CcaAsmHelper.COMPONENT_CONTAINER});

        for (Identifier componentId : staticComponentTypes) {
            MethodVisitor methodWriter = staticContainerWriter.visitMethod(Opcodes.ACC_PUBLIC, CcaAsmHelper.getStaticStorageGetterName(componentId), CcaAsmHelper.STATIC_CONTAINER_GETTER_DESC, null, null);
            methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
            // stack: <this>
            CcaAsmHelper.stackStaticComponentType(methodWriter, componentId);
            // stack: <this> componentType
            methodWriter.visitMethodInsn(Opcodes.INVOKEINTERFACE, CcaAsmHelper.COMPONENT_CONTAINER, "get", CcaAsmHelper.GET_DESC, true);
            // stack: component
            methodWriter.visitInsn(Opcodes.ARETURN);
            methodWriter.visitEnd();
        }

        staticContainerWriter.visitEnd();
        CcaAsmHelper.generateClass(staticContainerWriter);

        Map<Identifier, Class<? extends ComponentType<?>>> generatedComponentTypes = new HashMap<>(staticComponentTypes.size());
        for (Identifier componentId : staticComponentTypes) {
            /* generate the component type class */

            ClassNode componentTypeWriter = new ClassNode(CcaAsmHelper.ASM_VERSION);
            String componentTypeName = CcaAsmHelper.getComponentTypeName(componentId);
            componentTypeWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, componentTypeName, null, CcaAsmHelper.COMPONENT_TYPE, null);

            MethodVisitor init = componentTypeWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", COMPONENT_TYPE_INIT_DESC, null, null);
            init.visitCode();
            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitVarInsn(Opcodes.ALOAD, 1);
            init.visitVarInsn(Opcodes.ALOAD, 2);
            init.visitVarInsn(Opcodes.ILOAD, 3);
            init.visitMethodInsn(Opcodes.INVOKESPECIAL, CcaAsmHelper.COMPONENT_TYPE, "<init>", COMPONENT_TYPE_INIT_DESC, false);
            init.visitInsn(Opcodes.RETURN);
            init.visitEnd();

            MethodVisitor get = componentTypeWriter.visitMethod(Opcodes.ACC_PROTECTED, "getNullable", COMPONENT_TYPE_GET0_DESC, null, null);
            get.visitCode();
            get.visitVarInsn(Opcodes.ALOAD, 1);
            // stack: componentProvider
            get.visitMethodInsn(Opcodes.INVOKEINTERFACE, CcaAsmHelper.COMPONENT_PROVIDER, "getStaticComponentContainer", "()Ljava/lang/Object;", true);
            // stack: object
            get.visitInsn(Opcodes.DUP);
            // stack: object object
            Label label = new Label();
            get.visitJumpInsn(Opcodes.IFNULL, label);
            // stack: object
            get.visitTypeInsn(Opcodes.CHECKCAST, CcaAsmHelper.STATIC_COMPONENT_CONTAINER);
            // stack: generatedComponentContainer
            get.visitMethodInsn(Opcodes.INVOKEINTERFACE, CcaAsmHelper.STATIC_COMPONENT_CONTAINER, CcaAsmHelper.getStaticStorageGetterName(componentId), CcaAsmHelper.STATIC_CONTAINER_GETTER_DESC, true);
            // stack: component
            get.visitInsn(Opcodes.ARETURN);
            // if the native component container is null, we use the classic runtime way
            get.visitLabel(label);
            // stack: object(null)
            get.visitInsn(Opcodes.POP); // pop the useless duplicated null
            // empty stack
            get.visitVarInsn(Opcodes.ALOAD, 1);
            // stack: componentProvider
            get.visitVarInsn(Opcodes.ALOAD, 0);
            // stack: componentProvider <this>
            get.visitMethodInsn(Opcodes.INVOKEINTERFACE, CcaAsmHelper.COMPONENT_PROVIDER, "getComponent", "(L" + CcaAsmHelper.COMPONENT_TYPE + ";)L" + CcaAsmHelper.COMPONENT +";", true);
            // stack: component
            get.visitInsn(Opcodes.ARETURN);
            get.visitEnd();

            @SuppressWarnings("unchecked") Class<? extends ComponentType<?>> ct = (Class<? extends ComponentType<?>>) CcaAsmHelper.generateClass(componentTypeWriter);
            generatedComponentTypes.put(componentId, ct);
        }
        return generatedComponentTypes;
    }

}
