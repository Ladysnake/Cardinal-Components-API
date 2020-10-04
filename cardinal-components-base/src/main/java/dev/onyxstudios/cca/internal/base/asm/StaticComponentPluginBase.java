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

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import nerdhub.cardinal.components.api.ComponentRegistry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class StaticComponentPluginBase<T, I, F> extends LazyDispatcher {

    private static final String EVENT_DESC = Type.getDescriptor(Event.class);
    private static final String EVENT$INVOKER_DESC;


    static {
        try {
            EVENT$INVOKER_DESC = Type.getMethodDescriptor(Event.class.getMethod("invoker"));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to find one or more method descriptors", e);
        }
    }

    private final Class<? super F> componentFactoryType;
    private final Map<ComponentKey<?>, F> componentFactories = new LinkedHashMap<>();
    private final Map<ComponentKey<?>, Class<? extends Component>> componentImpls = new LinkedHashMap<>();
    protected final Class<T> providerClass;
    protected final String implSuffix;
    private Class<? extends DynamicContainerFactory<T>> containerFactoryClass;

    protected StaticComponentPluginBase(String likelyInitTrigger, Class<T> providerClass, Class<? super F> componentFactoryType, String implSuffix) {
        super(likelyInitTrigger);
        this.componentFactoryType = componentFactoryType;
        this.providerClass = providerClass;
        this.implSuffix = implSuffix;
    }

    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @SuppressWarnings("unused")
    public static <I, C extends Component> Class<? extends ComponentContainer> spinComponentContainer(Class<? super I> componentFactoryType, Class<? super C> componentClass, Map<Identifier, I> componentFactories, String implNameSuffix) throws IOException {
        return CcaAsmHelper.spinComponentContainer(componentFactoryType, componentFactories.entrySet().stream().collect(Collectors.toMap(entry -> ComponentRegistryV3.INSTANCE.get(entry.getKey()), Map.Entry::getValue)), implNameSuffix);
    }

    /**
     * Defines an implementation of {@code I} which creates component containers of
     * the given implementation type, using an argument of the given {@code factoryArg} type.
     *
     * <p>The generated class has a single constructor, taking {@code eventCount} parameters of type {@link Event}.
     *
     * @param implNameSuffix       a unique suffix for the generated class
     * @param containerFactoryType the factory interface that is to be implemented by the returned class
     * @param containerImpl        the type of containers that is to be instantiated by the generated factory
     * @param actualFactoryParams  the actual type of the arguments taken by the {@link ComponentContainer} constructor
     */
    public static <I> Class<? extends I> spinContainerFactory(String implNameSuffix, Class<? super I> containerFactoryType, Class<? extends ComponentContainer> containerImpl, Class<?>... actualFactoryParams) throws IOException {
        CcaBootstrap.INSTANCE.ensureInitialized();

        CcaAsmHelper.checkValidJavaIdentifier(implNameSuffix);
        Constructor<?>[] constructors = containerImpl.getConstructors();

        if (constructors.length != 1) {
            throw new IllegalStateException("Ambiguous constructor declarations in " + containerImpl + ": " + Arrays.toString(constructors));
        }

        Method factorySam = CcaAsmHelper.findSam(containerFactoryType);

        if (factorySam.getParameterCount() != actualFactoryParams.length) {
            throw new IllegalArgumentException("Actual argument list length mismatches with factory SAM: " + Arrays.toString(actualFactoryParams) + " and " + factorySam);
        }

        // constructor has one more argument, for expected size
        if (constructors[0].getParameterCount() != factorySam.getParameterCount() + 1) {
            throw new IllegalArgumentException("Factory SAM parameter count should be one less than container constructor (found " + factorySam + " for " + constructors[0] + ")");
        }

        Type[] factoryArgs;
        {
            Class<?>[] factoryParamClasses = factorySam.getParameterTypes();
            factoryArgs = new Type[factoryParamClasses.length];

            for (int i = 0; i < factoryParamClasses.length; i++) {
                if (!factoryParamClasses[i].isAssignableFrom(actualFactoryParams[i])) {
                    throw new IllegalArgumentException("Container factory parameter " + factoryParamClasses[i].getSimpleName() + " is not assignable from specified actual parameter " + actualFactoryParams[i].getSimpleName() + "(" + factorySam + ", " + Arrays.toString(actualFactoryParams) + ")");
                }
                factoryArgs[i] = Type.getType(factoryParamClasses[i]);
            }
        }

        String containerCtorDesc = Type.getConstructorDescriptor(constructors[0]);
        String containerImplName = Type.getInternalName(containerImpl);
        ClassNode containerFactoryWriter = new ClassNode(CcaAsmHelper.ASM_VERSION);
        String factoryImplName = CcaAsmHelper.STATIC_CONTAINER_FACTORY + '_' + implNameSuffix;
        containerFactoryWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, factoryImplName, null, "java/lang/Object", new String[]{Type.getInternalName(containerFactoryType)});
        containerFactoryWriter.visitField(Opcodes.ACC_PRIVATE, "expectedSize", "I", null, 0);
        MethodVisitor init = containerFactoryWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(" + ")V", null, null);
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        init.visitInsn(Opcodes.RETURN);
        init.visitEnd();
        MethodVisitor createContainer = containerFactoryWriter.visitMethod(Opcodes.ACC_PUBLIC, factorySam.getName(), Type.getMethodDescriptor(factorySam), null, null);
        createContainer.visitTypeInsn(Opcodes.NEW, containerImplName);
        createContainer.visitInsn(Opcodes.DUP);
        createContainer.visitVarInsn(Opcodes.ALOAD, 0);
        // stack: <this>
        createContainer.visitFieldInsn(Opcodes.GETFIELD, factoryImplName, "expectedSize", "I");
        // stack: this.expectedSize
        for (int i = 0; i < actualFactoryParams.length; i++) {
            createContainer.visitVarInsn(factoryArgs[i].getOpcode(Opcodes.ILOAD), i + 1);
            if (factoryArgs[i].getSort() == Type.OBJECT || factoryArgs[i].getSort() == Type.ARRAY) {
                createContainer.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(actualFactoryParams[i]));
            }
        }
        // stack: this.expectedSize actualFactoryArgs...
        createContainer.visitMethodInsn(Opcodes.INVOKESPECIAL, containerImplName, "<init>", containerCtorDesc, false);
        // stack: container
        createContainer.visitInsn(Opcodes.ARETURN);
        createContainer.visitEnd();
        containerFactoryWriter.visitEnd();
        @SuppressWarnings("unchecked") Class<? extends I> ret = (Class<? extends I>) CcaAsmHelper.generateClass(containerFactoryWriter);
        return ret;
    }

    public static ComponentContainer createEmptyContainer(String implSuffix) {
        try {
            Class<? extends ComponentContainer> containerCls = CcaAsmHelper.spinComponentContainer(Runnable.class, Collections.emptyMap(), implSuffix);
            return containerCls.getConstructor(int.class).newInstance(0);
        } catch (IOException | ReflectiveOperationException e) {
            throw new StaticComponentLoadingException("Failed to generate empty component container", e);
        }
    }

    @SuppressWarnings("unused")
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public static <C extends Component> ComponentContainer createEmptyContainer(Class<? super C> componentClass, String implSuffix) {
        return createEmptyContainer(implSuffix);
    }

    public Class<? extends DynamicContainerFactory<T>> getContainerFactoryClass() {
        this.ensureInitialized();

        return this.containerFactoryClass;
    }

    @Override
    protected void init() {
        processInitializers(this.getEntrypoints(), this::dispatchRegistration);

        try {
            Class<? extends ComponentContainer> containerCls = CcaAsmHelper.spinComponentContainer(
                this.componentFactoryType,
                this.componentFactories,
                this.componentImpls,
                this.implSuffix
            );
            this.containerFactoryClass = this.spinContainerFactory(containerCls);
        } catch (IOException e) {
            throw new StaticComponentLoadingException("Failed to generate a dedicated component container for " + this.providerClass, e);
        }
    }

    protected Class<? extends DynamicContainerFactory<T>> spinContainerFactory(Class<? extends ComponentContainer> containerCls) throws IOException {
        return spinContainerFactory(this.implSuffix, DynamicContainerFactory.class, containerCls, this.providerClass);
    }

    public static <I> void processInitializers(Collection<EntrypointContainer<I>> entrypoints, Consumer<I> action) {
        for (EntrypointContainer<I> entrypoint : entrypoints) {
            try {
                action.accept(entrypoint.getEntrypoint());
            } catch (Throwable e) {
                ModMetadata metadata = entrypoint.getProvider().getMetadata();
                throw new StaticComponentLoadingException(String.format("Exception while registering static component factories for %s (%s)", metadata.getName(), metadata.getId()), e);
            }
        }
    }

    protected abstract Collection<EntrypointContainer<I>> getEntrypoints();

    protected abstract void dispatchRegistration(I entrypoint);

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    protected void register(Identifier componentId, F factory) {
        this.register(Objects.requireNonNull(ComponentRegistry.INSTANCE.get(componentId)), factory);
    }

    protected void register(ComponentKey<?> key, F factory) {
        this.register(key, key.getComponentClass(), factory);
    }

    protected void register(ComponentKey<?> key, Class<? extends Component> impl, F factory) {
        this.componentFactories.put(key, factory);
        this.componentImpls.put(key, impl);
    }
}
