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
package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.internal.asm.CcaAsmConstants;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.FactoryClassScanner;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.api.metadata.CustomValue;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

public final class CcaBootstrap implements PreLaunchEntrypoint {

    public static final CcaBootstrap INSTANCE = new CcaBootstrap();
    public static final String COMPONENT_TYPE_INIT_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(CcaAsmConstants.IDENTIFIER), Type.getType(Class.class), Type.INT_TYPE);
    public static final String COMPONENT_TYPE_GET0_DESC = "(L" + CcaAsmConstants.COMPONENT_PROVIDER + ";)L" + CcaAsmConstants.COMPONENT + ";";

    private final Map</*Identifier*/ String, Class<? extends ComponentType<?>>> generatedComponentTypes = new HashMap<>();

    @Nullable
    public Class<? extends ComponentType<?>> getGeneratedComponentTypeClass(String componentId) {
        return this.generatedComponentTypes.get(componentId);
    }

    @Override
    public void onPreLaunch() {
        try {
            List<StaticComponentPlugin> staticProviders = FabricLoader.getInstance().getEntrypoints("cardinal-components-api:static-provider", StaticComponentPlugin.class);
            Map<String, StaticComponentPlugin> staticProviderAnnotations = this.collectAnnotations(staticProviders);
            Set<String> staticComponentTypes = this.process(staticProviderAnnotations);
            this.generatedComponentTypes.putAll(this.defineStaticComponentContainer(staticComponentTypes));
            this.generateSpecializedContainers(staticProviders);
        } catch (IOException | UncheckedIOException e) {
            throw new StaticComponentLoadingException("Failed to load statically defined components", e);
        } finally {
            CcaAsmHelper.clearCache();
        }
    }

    private void generateSpecializedContainers(List<StaticComponentPlugin> staticProviders) {
        for (StaticComponentPlugin staticProvider : staticProviders) {
            try {
                staticProvider.generate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Nonnull
    private Set<String> process(Map<String, StaticComponentPlugin> staticProviderAnnotations) throws IOException {
        Set</*Identifier*/ String> staticComponentTypes = new HashSet<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            CustomValue factories = mod.getMetadata().getCustomValue("cardinal-components-api:static-factories");
            if (factories == null) continue;
            for (CustomValue factory : factories.getAsArray()) {
                this.process(factory.getAsString(), staticProviderAnnotations, staticComponentTypes);
            }
        }
        return staticComponentTypes;
    }

    private void process(String className, Map<String, StaticComponentPlugin> staticProviderAnnotations, Set<String> staticComponentTypes) throws IOException {
        ClassReader reader = CcaAsmHelper.getClassReader(Type.getObjectType(className.replace('.', '/')));
        ClassVisitor adapter = new FactoryClassScanner(CcaAsmConstants.ASM_VERSION, null, staticProviderAnnotations, staticComponentTypes);
        reader.accept(adapter, 0);
    }

    @Nonnull
    private Map<String, StaticComponentPlugin> collectAnnotations(List<StaticComponentPlugin> staticProviders) {
        Map</*Class<? extends Annotation>*/ String, StaticComponentPlugin> staticProviderAnnotations = new HashMap<>();
        for (StaticComponentPlugin staticProvider : staticProviders) {
            staticProviderAnnotations.put(Type.getDescriptor(staticProvider.getAnnotationType()), staticProvider);
        }
        return staticProviderAnnotations;
    }

    private Map<String, Class<? extends ComponentType<?>>> defineStaticComponentContainer(Set<String> staticComponentTypes) {
        ClassNode staticContainerWriter = new ClassNode(CcaAsmConstants.ASM_VERSION);
        ClassNode staticComponentTypesNode = new ClassNode(CcaAsmConstants.ASM_VERSION);
        class ComponentTypeWriter {
            private final ClassNode node;
            private final String identifier;
            private final String name;

            private ComponentTypeWriter(ClassNode node, String identifier, String name) {
                this.node = node;
                this.identifier = identifier;
                this.name = name;
            }
        }
        List<ComponentTypeWriter> componentTypeWriters = new ArrayList<>(staticComponentTypes.size());
        staticContainerWriter.visit(Opcodes.V1_8, Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE, CcaAsmConstants.STATIC_COMPONENT_CONTAINER, null, "java/lang/Object", new String[]{CcaAsmConstants.COMPONENT_CONTAINER});
        staticComponentTypesNode.visit(Opcodes.V1_8, Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC, CcaAsmConstants.STATIC_COMPONENT_TYPES, null, "java/lang/Object", null);
        MethodVisitor componentTypesInit = staticComponentTypesNode.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        for (String identifier : staticComponentTypes) {
            /* generate the component type class */

            ClassNode componentTypeWriter = new ClassNode(CcaAsmConstants.ASM_VERSION);
            String componentTypeName = CcaAsmConstants.getComponentTypeName(identifier);
            componentTypeWriters.add(new ComponentTypeWriter(componentTypeWriter, identifier, componentTypeName));
            componentTypeWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, componentTypeName, null, CcaAsmConstants.COMPONENT_TYPE, null);

            MethodVisitor init = componentTypeWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", COMPONENT_TYPE_INIT_DESC, null, null);
            init.visitCode();
            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitVarInsn(Opcodes.ALOAD, 1);
            init.visitVarInsn(Opcodes.ALOAD, 2);
            init.visitVarInsn(Opcodes.ILOAD, 3);
            init.visitMethodInsn(Opcodes.INVOKESPECIAL, CcaAsmConstants.COMPONENT_TYPE, "<init>", COMPONENT_TYPE_INIT_DESC, false);
            init.visitInsn(Opcodes.RETURN);
            init.visitEnd();

            MethodVisitor get = componentTypeWriter.visitMethod(Opcodes.ACC_PROTECTED, "get0", COMPONENT_TYPE_GET0_DESC, null, null);
            get.visitCode();
            get.visitVarInsn(Opcodes.ALOAD, 1);
            // stack: componentProvider
            get.visitMethodInsn(Opcodes.INVOKEINTERFACE, CcaAsmConstants.COMPONENT_PROVIDER, "getStaticComponentContainer", "()Ljava/lang/Object;", true);
            // stack: object
            get.visitInsn(Opcodes.DUP);
            // stack: object object
            Label label = new Label();
            get.visitJumpInsn(Opcodes.IFNULL, label);
            // stack: object
            get.visitTypeInsn(Opcodes.CHECKCAST, CcaAsmConstants.STATIC_COMPONENT_CONTAINER);
            // stack: generatedComponentContainer
            get.visitMethodInsn(Opcodes.INVOKEINTERFACE, CcaAsmConstants.STATIC_COMPONENT_CONTAINER, CcaAsmConstants.getStaticStorageGetterName(identifier), CcaAsmConstants.STATIC_CONTAINER_GETTER_DESC, true);
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
            get.visitMethodInsn(Opcodes.INVOKEINTERFACE, CcaAsmConstants.COMPONENT_PROVIDER, "getComponent", "(L" + CcaAsmConstants.COMPONENT_TYPE + ";)L" + CcaAsmConstants.COMPONENT +";", true);
            // stack: component
            get.visitInsn(Opcodes.ARETURN);
            get.visitEnd();

            /* generate a Lazy field in StaticComponentTypes */

            String typeConstantName = CcaAsmConstants.getTypeConstantName(identifier);
            staticComponentTypesNode.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, typeConstantName, "L" + CcaAsmConstants.LAZY_COMPONENT_TYPE + ";", null, null);
            componentTypesInit.visitLdcInsn(identifier);
            componentTypesInit.visitMethodInsn(Opcodes.INVOKESTATIC, CcaAsmConstants.LAZY_COMPONENT_TYPE, "create", "(Ljava/lang/String;)L" + CcaAsmConstants.LAZY_COMPONENT_TYPE + ";", false);
            componentTypesInit.visitFieldInsn(Opcodes.PUTSTATIC, CcaAsmConstants.STATIC_COMPONENT_TYPES, typeConstantName, "L" + CcaAsmConstants.LAZY_COMPONENT_TYPE + ";");

            /* generate the component container getter */

            MethodVisitor methodWriter = staticContainerWriter.visitMethod(Opcodes.ACC_PUBLIC, CcaAsmConstants.getStaticStorageGetterName(identifier), CcaAsmConstants.STATIC_CONTAINER_GETTER_DESC, null, null);
            methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
            // stack: <this>
            // get the generated lazy component type constant
            methodWriter.visitFieldInsn(Opcodes.GETSTATIC, CcaAsmConstants.STATIC_COMPONENT_TYPES, CcaAsmConstants.getTypeConstantName(identifier), "L" + CcaAsmConstants.LAZY_COMPONENT_TYPE + ";");
            // stack: <this> lazyComponentType
            methodWriter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CcaAsmConstants.LAZY_COMPONENT_TYPE, "get", "()L" + CcaAsmConstants.COMPONENT_TYPE + ";", false);
            // stack: <this> componentType
            methodWriter.visitMethodInsn(Opcodes.INVOKEINTERFACE, CcaAsmConstants.COMPONENT_CONTAINER, "get", "(L" + CcaAsmConstants.COMPONENT_TYPE + ";)L" + CcaAsmConstants.COMPONENT + ";", true);
            // stack: component
            methodWriter.visitInsn(Opcodes.ARETURN);
            methodWriter.visitEnd();
        }
        staticContainerWriter.visitEnd();
        CcaAsmHelper.generateClass(staticContainerWriter, CcaAsmConstants.STATIC_COMPONENT_CONTAINER);
        componentTypesInit.visitInsn(Opcodes.RETURN);
        componentTypesInit.visitEnd();
        Map<String, Class<? extends ComponentType<?>>> generatedComponentTypes = new HashMap<>(componentTypeWriters.size());
        for (ComponentTypeWriter componentTypeWriter : componentTypeWriters) {
            @SuppressWarnings("unchecked") Class<? extends ComponentType<?>> ct = (Class<? extends ComponentType<?>>) CcaAsmHelper.generateClass(componentTypeWriter.node, componentTypeWriter.name);
            generatedComponentTypes.put(componentTypeWriter.identifier, ct);
        }
        CcaAsmHelper.generateClass(staticComponentTypesNode, CcaAsmConstants.STATIC_COMPONENT_TYPES);
        return generatedComponentTypes;
    }

}
