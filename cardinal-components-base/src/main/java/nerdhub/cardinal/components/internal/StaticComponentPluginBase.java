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
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.util.container.FastComponentContainer;
import nerdhub.cardinal.components.internal.asm.AnnotationData;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.MethodData;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class StaticComponentPluginBase implements StaticComponentPlugin {
    private final Map<String, MethodData> componentFactories = new HashMap<>();
    private final String providerClass;
    private final String implSuffix;
    private final Class<? extends Annotation> annotationType;
    private Class<? extends FeedbackContainerFactory<?, ?>> factoryClass;

    protected StaticComponentPluginBase(String className, String implSuffix, Class<? extends Annotation> annotationType) {
        this.providerClass = className;
        this.implSuffix = implSuffix;
        this.annotationType = annotationType;
    }

    /**
     * Defines an implementation of {@link ComponentContainer} that supports direct component access.
     *
     * <p>Instances of the returned class can be returned by {@link ComponentProvider#getStaticComponentContainer()}.
     * <strong>This method must not be called before the static component container interface has been defined!</strong>
     * It is normally called in {@link StaticComponentPlugin#generate()}.
     *
     * <p>Generated component container classes will take an additional {@code int} as first argument to their
     * constructors. That number corresponds to the expected dynamic size of the container (see {@link FastComponentContainer}).
     *
     * @param componentFactories a map of {@link ComponentType} ids to factories for components of that type
     * @param implNameSuffix a unique suffix for the generated class
     * @param ctorArgs arguments taken by the generated class' constructor, forwarded to each factory.
     *                 If a factory takes {@code n} arguments where {@code n < ctorArgs.length}, only the first {@code n}
     *                 arguments will be passed to it. <em>It is the responsibility of the caller to ensure the factories'
     *                 arguments are valid.</em>
     * @return the generated container class
     */
    public static Class<? extends ComponentContainer<?>> defineContainer(Map<String, MethodData> componentFactories, String implNameSuffix, Type... ctorArgs) throws IOException {
        checkValidJavaIdentifier(implNameSuffix);
        String containerImplName = CcaAsmHelper.STATIC_COMPONENT_CONTAINER + '_' + implNameSuffix;
        Type[] actualCtorArgs = new Type[ctorArgs.length + 1];
        actualCtorArgs[0] = Type.INT_TYPE;
        System.arraycopy(ctorArgs, 0, actualCtorArgs, 1, ctorArgs.length);
        String ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, actualCtorArgs);
        int ctorFirstAvailableVar = actualCtorArgs.length + 1;  // explicit args + <this>
        ClassNode classNode = new ClassNode(CcaAsmHelper.ASM_VERSION);
        classNode.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
            containerImplName,
            null,
            CcaAsmHelper.DYNAMIC_COMPONENT_CONTAINER_IMPL,
            new String[] {CcaAsmHelper.STATIC_COMPONENT_CONTAINER}
        );
        MethodVisitor init = classNode.visitMethod(Opcodes.ACC_PUBLIC, "<init>", ctorDesc, null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitVarInsn(Opcodes.ILOAD, 1);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, CcaAsmHelper.DYNAMIC_COMPONENT_CONTAINER_IMPL, "<init>", "(I)V", false);
        MethodVisitor get = classNode.visitMethod(Opcodes.ACC_PUBLIC, "get", "(L" + CcaAsmHelper.COMPONENT_TYPE + ";)L" + CcaAsmHelper.COMPONENT + ";", null, null);
        MethodVisitor forEach = classNode.visitMethod(Opcodes.ACC_PUBLIC, "forEach", "(Ljava/util/function/BiConsumer;)V", null, null);
        String canBeAssignedDesc = "(L" + CcaAsmHelper.COMPONENT_TYPE + ";)Z";
        MethodVisitor canBeAssigned = classNode.visitMethod(Opcodes.ACC_PROTECTED, "canBeAssigned", canBeAssignedDesc, null, null);
        Label canBeAssignedFalse = new Label();
        for (Map.Entry<String, MethodData> entry : componentFactories.entrySet()) {
            String identifier = entry.getKey();
            MethodData factory = entry.getValue();
            String fieldName = CcaAsmHelper.getJavaIdentifierName(identifier);
            String fieldDescriptor = "L" + CcaAsmHelper.COMPONENT + ";";
            /* field declaration */
            classNode.visitField(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                fieldName,
                fieldDescriptor,
                null,
                null
            ).visitEnd();
            /* constructor initialization */
            Type[] factoryArgs = factory.descriptor.getArgumentTypes();
            for (int i = 0; i < ctorArgs.length && i < factoryArgs.length; i++) {
                init.visitVarInsn(Opcodes.ALOAD, i + 2);    // first 2 args are for the container itself
                init.visitTypeInsn(Opcodes.CHECKCAST, factoryArgs[i].getInternalName());
            }
            // stack: ctorArgs...
            // initialize the component by calling the factory
            init.visitMethodInsn(Opcodes.INVOKESTATIC, factory.ownerType.getInternalName(), factory.name, factory.descriptor.toString(), false);
            // stack: component
            init.visitInsn(Opcodes.DUP);
            // stack: component component
            init.visitVarInsn(Opcodes.ASTORE, ctorFirstAvailableVar);
            // stack: component
            // if the factory method returned null, we just ignore it
            Label nextInit = new Label();
            init.visitJumpInsn(Opcodes.IFNULL, nextInit);
            // <empty stack>
            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitInsn(Opcodes.DUP);
            init.visitVarInsn(Opcodes.ALOAD, ctorFirstAvailableVar);
            // stack: <this> <this> component
            // store in the field
            init.visitFieldInsn(Opcodes.PUTFIELD, containerImplName, fieldName, fieldDescriptor);
            // stack: <this>
            stackStaticComponentType(init, identifier);
            // stack: <this> componentType
            init.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CcaAsmHelper.DYNAMIC_COMPONENT_CONTAINER_IMPL, "addContainedType", "(L" + CcaAsmHelper.COMPONENT_TYPE + ";)V", false);
            // <empty stack>
            init.visitLabel(nextInit);

            /* get implementation */
            // note: this implementation is O(n). For best results, we could make a big lookup table based on
            Label nextGet = new Label();
            get.visitVarInsn(Opcodes.ALOAD, 1);
            stackStaticComponentType(get, identifier);
            get.visitJumpInsn(Opcodes.IF_ACMPNE, nextGet);
            stackStaticComponent(get, containerImplName, fieldName, fieldDescriptor);
            get.visitInsn(Opcodes.ARETURN);
            get.visitLabel(nextGet);

            /* forEach implementation */
            forEach.visitVarInsn(Opcodes.ALOAD, 1);
            // stack: biConsumer
            stackStaticComponentType(forEach, identifier);
            // stack: biConsumer componentType
            stackStaticComponent(forEach, containerImplName, fieldName, fieldDescriptor);
            // stack: biConsumer componentType component
            forEach.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(BiConsumer.class), "accept", "(Ljava/lang/Object;Ljava/lang/Object;)V", true);

            /* canBeAssigned implementation */
            canBeAssigned.visitVarInsn(Opcodes.ALOAD, 1);
            // stack: componentType
            stackStaticComponentType(canBeAssigned, identifier);
            // stack: componentType componentType2
            canBeAssigned.visitJumpInsn(Opcodes.IF_ACMPEQ, canBeAssignedFalse);
            // <empty stack>

            /* getter implementation */
            MethodVisitor getter = classNode.visitMethod(
                Opcodes.ACC_PUBLIC,
                CcaAsmHelper.getStaticStorageGetterName(identifier),
                CcaAsmHelper.STATIC_CONTAINER_GETTER_DESC,
                null,
                null
            );
            stackStaticComponent(getter, containerImplName, fieldName, fieldDescriptor);
            getter.visitInsn(Opcodes.ARETURN);
            getter.visitEnd();
        }
        init.visitInsn(Opcodes.RETURN);
        init.visitEnd();
        get.visitVarInsn(Opcodes.ALOAD, 0);
        get.visitVarInsn(Opcodes.ALOAD, 1);
        get.visitMethodInsn(Opcodes.INVOKESPECIAL, CcaAsmHelper.DYNAMIC_COMPONENT_CONTAINER_IMPL, "get", "(L" + CcaAsmHelper.COMPONENT_TYPE + ";)L" + CcaAsmHelper.COMPONENT + ";", false);
        get.visitInsn(Opcodes.ARETURN);
        get.visitEnd();
        forEach.visitVarInsn(Opcodes.ALOAD, 0);
        forEach.visitVarInsn(Opcodes.ALOAD, 1);
        forEach.visitMethodInsn(Opcodes.INVOKESPECIAL, CcaAsmHelper.DYNAMIC_COMPONENT_CONTAINER_IMPL, "forEach", "(L" + Type.getInternalName(BiConsumer.class) + ";)V", false);
        forEach.visitInsn(Opcodes.RETURN);
        forEach.visitEnd();
        canBeAssigned.visitVarInsn(Opcodes.ALOAD, 0);
        canBeAssigned.visitVarInsn(Opcodes.ALOAD, 1);
        canBeAssigned.visitMethodInsn(Opcodes.INVOKESPECIAL, CcaAsmHelper.DYNAMIC_COMPONENT_CONTAINER_IMPL, "canBeAssigned", canBeAssignedDesc, false);
        canBeAssigned.visitJumpInsn(Opcodes.IFEQ, canBeAssignedFalse);
        Label canBeAssignedEnd = new Label();
        canBeAssigned.visitInsn(Opcodes.ICONST_1);
        canBeAssigned.visitJumpInsn(Opcodes.GOTO, canBeAssignedEnd);
        canBeAssigned.visitLabel(canBeAssignedFalse);
        canBeAssigned.visitInsn(Opcodes.ICONST_0);
        canBeAssigned.visitLabel(canBeAssignedEnd);
        canBeAssigned.visitInsn(Opcodes.IRETURN);
        canBeAssigned.visitEnd();
        @SuppressWarnings("unchecked") Class<? extends ComponentContainer<?>> ret = (Class<? extends ComponentContainer<?>>) CcaAsmHelper.generateClass(classNode, containerImplName);
        return ret;
    }

    /**
     * Defines a subclass of {@link FeedbackContainerFactory} which creates component containers of
     * the given implementation type, using an argument of the given {@code factoryArg} type.
     *
     * <p>The generated class has the same constructor arguments as the original class {@link FeedbackContainerFactory}.
     *
     * @param implNameSuffix a unique suffix for the generated class
     * @param containerImpl  the implementation type of containers that is to be instantiated by the generated factories
     * @param factoryArg     the type of the single argument taken by the {@link ComponentContainer} constructor
     */
    public static Class<? extends FeedbackContainerFactory<?, ?>> defineSingleArgFactory(String implNameSuffix, Type containerImpl, Type factoryArg) throws IOException {
        checkValidJavaIdentifier(implNameSuffix);
        String containerImplName = containerImpl.getInternalName();
        ClassNode containerFactoryWriter = new ClassNode(CcaAsmHelper.ASM_VERSION);
        String factoryImplName = CcaAsmHelper.STATIC_CONTAINER_FACTORY + '_' + implNameSuffix;
        containerFactoryWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, factoryImplName, null, CcaAsmHelper.CONTAINER_FACTORY_IMPL, null);
        String ctorDesc = "([L" + CcaAsmHelper.EVENT + ";)V";
        MethodVisitor init = containerFactoryWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", ctorDesc, null, null);
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitVarInsn(Opcodes.ALOAD, 1);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, CcaAsmHelper.CONTAINER_FACTORY_IMPL, "<init>", ctorDesc, false);
        init.visitInsn(Opcodes.RETURN);
        init.visitEnd();
        MethodVisitor createContainer = containerFactoryWriter.visitMethod(Opcodes.ACC_PROTECTED, "createContainer", "(ILjava/lang/Object;)L" + CcaAsmHelper.DYNAMIC_COMPONENT_CONTAINER_IMPL + ";", null, null);
        createContainer.visitTypeInsn(Opcodes.NEW, containerImplName);
        createContainer.visitInsn(Opcodes.DUP);
        createContainer.visitVarInsn(Opcodes.ILOAD, 1);
        createContainer.visitVarInsn(Opcodes.ALOAD, 2);
        createContainer.visitTypeInsn(Opcodes.CHECKCAST, factoryArg.getInternalName());
        createContainer.visitMethodInsn(Opcodes.INVOKESPECIAL, containerImplName, "<init>", "(I" + factoryArg.getDescriptor() + ")V", false);
        createContainer.visitInsn(Opcodes.ARETURN);
        createContainer.visitEnd();
        containerFactoryWriter.visitEnd();
        @SuppressWarnings("unchecked") Class<? extends FeedbackContainerFactory<?, ?>> ret = (Class<? extends FeedbackContainerFactory<?, ?>>) CcaAsmHelper.generateClass(containerFactoryWriter, factoryImplName);
        return ret;
    }

    private static void checkValidJavaIdentifier(String implNameSuffix) {
        for (int i = 0; i < implNameSuffix.length(); i++) {
            if (!Character.isJavaIdentifierPart(implNameSuffix.charAt(i))) {
                throw new IllegalArgumentException(implNameSuffix + " is not a valid suffix for a java identifier");
            }
        }
    }

    private static void stackStaticComponent(MethodVisitor method, String containerImplName, String fieldName, String fieldDescriptor) {
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD, containerImplName, fieldName, fieldDescriptor);
    }

    private static void stackStaticComponentType(MethodVisitor method, String componentId) {
        // get the generated lazy component type constant
        method.visitFieldInsn(Opcodes.GETSTATIC, CcaAsmHelper.STATIC_COMPONENT_TYPES, CcaAsmHelper.getTypeConstantName(componentId), "L" + CcaAsmHelper.LAZY_COMPONENT_TYPE + ";");
        // stack: <this> component lazyComponentType
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CcaAsmHelper.LAZY_COMPONENT_TYPE, "get", "()L" + CcaAsmHelper.COMPONENT_TYPE + ";", false);
    }

    public Class<? extends FeedbackContainerFactory<?, ?>> getFactoryClass() {
        return Objects.requireNonNull(this.factoryClass, "PreLaunch not fired ?!");
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return this.annotationType;
    }

    @ApiStatus.OverrideOnly
    @Override
    public String scan(MethodData factory, AnnotationData annotation) {
        if (factory.descriptor.getArgumentTypes().length > 1) {
            throw new StaticComponentLoadingException("Too many arguments in method " + factory + ". Should be either no-args or a single " + this.providerClass + " argument.");
        }
        String value = annotation.get("value", String.class);
        this.componentFactories.put(value, factory);
        return value;
    }

    @ApiStatus.OverrideOnly
    @Override
    public void generate() throws IOException {
        Type levelType = Type.getObjectType(this.providerClass.replace('.', '/'));
        Class<? extends ComponentContainer<?>> containerCls = defineContainer(this.componentFactories, this.implSuffix, levelType);
        this.factoryClass = defineSingleArgFactory(this.implSuffix, Type.getType(containerCls), levelType);
    }
}
