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
package nerdhub.cardinal.components.internal.asm;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.FeedbackContainerFactory;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class CcaAsmHelper {
    private static final Map<Type, TypeData> typeCache = new HashMap<>();

    private static TypeData getTypeData(Type type) throws IOException {
        TypeData t = typeCache.get(type);
        if (t != null) {
            return t;
        }
        String className = type.getInternalName();
        ClassReader reader = new ClassReader(FabricLauncherBase.getLauncher().getClassByteArray(className.replace('.', '/')));
        TypeData newValue = new TypeData(type, Type.getObjectType(reader.getSuperName()), reader);
        typeCache.put(type, newValue);
        return newValue;
    }

    public static Type getSuperclass(Type type) throws IOException {
        return getTypeData(type).getSupertype();
    }

    public static boolean isAssignableFrom(Type tSuper, Type tSub) throws IOException {
        if (tSuper.equals(tSub)) return true;
        if (tSub.equals(Type.getType(Object.class))) return false;
        return isAssignableFrom(tSuper, getSuperclass(tSub));
    }

    public static ClassReader getClassReader(Type type) throws IOException {
        return getTypeData(type).getReader();
    }

    public static Class<? extends ComponentContainer<?>> defineContainer(Map<String, NamedMethodDescriptor> componentFactories, String implNameSuffix, Type... ctorArgs) {
        String containerImplName = CcaAsmConstants.STATIC_COMPONENT_CONTAINER + '_' + implNameSuffix;
        Type[] actualCtorArgs = new Type[ctorArgs.length + 1];
        actualCtorArgs[0] = Type.INT_TYPE;
        System.arraycopy(ctorArgs, 0, actualCtorArgs, 1, ctorArgs.length);
        String ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, actualCtorArgs);
        ClassNode classNode = new ClassNode(CcaAsmConstants.ASM_VERSION);
        classNode.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
            containerImplName,
            null,
            CcaAsmConstants.DYNAMIC_COMPONENT_CONTAINER_IMPL,
            new String[] {CcaAsmConstants.STATIC_COMPONENT_CONTAINER}
        );
        MethodVisitor init = classNode.visitMethod(Opcodes.ACC_PUBLIC, "<init>", ctorDesc, null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitVarInsn(Opcodes.ILOAD, 1);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, CcaAsmConstants.DYNAMIC_COMPONENT_CONTAINER_IMPL, "<init>", "(I)V", false);
        MethodVisitor get = classNode.visitMethod(Opcodes.ACC_PUBLIC, "get", "(L" + CcaAsmConstants.COMPONENT_TYPE + ";)L" + CcaAsmConstants.COMPONENT + ";", null, null);
        MethodVisitor forEach = classNode.visitMethod(Opcodes.ACC_PUBLIC, "forEach", "(Ljava/util/function/BiConsumer;)V", null, null);
        for (Map.Entry<String, NamedMethodDescriptor> entry : componentFactories.entrySet()) {
            String identifier = entry.getKey();
            NamedMethodDescriptor factory = entry.getValue();
            String fieldName = CcaAsmConstants.getJavaIdentifierName(identifier);
            String fieldDescriptor = "L" + CcaAsmConstants.COMPONENT + ";";
            /* field declaration */
            classNode.visitField(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                fieldName,
                fieldDescriptor,
                null,
                null
            ).visitEnd();
            /* constructor initialization */
            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitInsn(Opcodes.DUP);
            // stack: <this>
            for (int i = 0; i < ctorArgs.length && i < factory.args.length; i++) {
                init.visitVarInsn(Opcodes.ALOAD, i + 2);    // first 2 args are for the container itself
                init.visitTypeInsn(Opcodes.CHECKCAST, factory.args[i].getInternalName());
            }
            // stack: <this> ctorArgs...
            // initialize the component by calling the factory
            init.visitMethodInsn(Opcodes.INVOKESTATIC, factory.ownerType.getInternalName(), factory.name, factory.descriptor.toString(), false);
            // stack: <this> component
            // store in the field
            init.visitFieldInsn(Opcodes.PUTFIELD, containerImplName, fieldName, fieldDescriptor);
            // stack: <this>
            stackStaticComponentType(init, identifier);
            // stack: <this> componentType
            init.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CcaAsmConstants.DYNAMIC_COMPONENT_CONTAINER_IMPL, "addContainedType", "(L" + CcaAsmConstants.COMPONENT_TYPE + ";)V", false);
            // <empty stack>

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

            /* getter implementation */
            MethodVisitor getter = classNode.visitMethod(
                Opcodes.ACC_PUBLIC,
                CcaAsmConstants.getStaticStorageGetterName(identifier),
                CcaAsmConstants.STATIC_CONTAINER_GETTER_DESC,
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
        get.visitMethodInsn(Opcodes.INVOKESPECIAL, CcaAsmConstants.DYNAMIC_COMPONENT_CONTAINER_IMPL, "get", "(L" + CcaAsmConstants.COMPONENT_TYPE + ";)L" + CcaAsmConstants.COMPONENT + ";", false);
        get.visitInsn(Opcodes.ARETURN);
        get.visitEnd();
        forEach.visitVarInsn(Opcodes.ALOAD, 0);
        forEach.visitVarInsn(Opcodes.ALOAD, 1);
        forEach.visitMethodInsn(Opcodes.INVOKESPECIAL, CcaAsmConstants.DYNAMIC_COMPONENT_CONTAINER_IMPL, "forEach", "(L" + Type.getInternalName(BiConsumer.class) + ";)V", false);
        forEach.visitInsn(Opcodes.RETURN);
        forEach.visitEnd();
        @SuppressWarnings("unchecked") Class<? extends ComponentContainer<?>> ret = (Class<? extends ComponentContainer<?>>) generateClass(classNode, containerImplName);
        return ret;
    }

    private static void stackStaticComponent(MethodVisitor method, String containerImplName, String fieldName, String fieldDescriptor) {
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD, containerImplName, fieldName, fieldDescriptor);
    }

    public static void stackStaticComponentType(MethodVisitor method, String componentId) {
        // get the generated lazy component type constant
        method.visitFieldInsn(Opcodes.GETSTATIC, CcaAsmConstants.STATIC_COMPONENT_TYPES, CcaAsmConstants.getTypeConstantName(componentId), "L" + CcaAsmConstants.LAZY_COMPONENT_TYPE + ";");
        // stack: <this> component lazyComponentType
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CcaAsmConstants.LAZY_COMPONENT_TYPE, "get", "()L" + CcaAsmConstants.COMPONENT_TYPE + ";", false);
    }

    public static Class<? extends FeedbackContainerFactory<?, ?>> defineSingleArgFactory(String implNameSuffix, Type containerImpl, Type factoryArg) {
        String containerImplName = containerImpl.getInternalName();
        ClassNode containerFactoryWriter = new ClassNode(CcaAsmConstants.ASM_VERSION);
        String factoryImplName = CcaAsmConstants.STATIC_CONTAINER_FACTORY + '_' + implNameSuffix;
        containerFactoryWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, factoryImplName, null, CcaAsmConstants.CONTAINER_FACTORY_IMPL, null);
        String ctorDesc = "([L" + CcaAsmConstants.EVENT + ";)V";
        MethodVisitor init = containerFactoryWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", ctorDesc, null, null);
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitVarInsn(Opcodes.ALOAD, 1);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, CcaAsmConstants.CONTAINER_FACTORY_IMPL, "<init>", ctorDesc, false);
        init.visitInsn(Opcodes.RETURN);
        init.visitEnd();
        MethodVisitor createContainer = containerFactoryWriter.visitMethod(Opcodes.ACC_PROTECTED, "createContainer", "(ILjava/lang/Object;)L" + CcaAsmConstants.DYNAMIC_COMPONENT_CONTAINER_IMPL + ";", null, null);
        createContainer.visitTypeInsn(Opcodes.NEW, containerImplName);
        createContainer.visitInsn(Opcodes.DUP);
        createContainer.visitVarInsn(Opcodes.ILOAD, 1);
        createContainer.visitVarInsn(Opcodes.ALOAD, 2);
        createContainer.visitTypeInsn(Opcodes.CHECKCAST, factoryArg.getInternalName());
        createContainer.visitMethodInsn(Opcodes.INVOKESPECIAL, containerImplName, "<init>", "(I" + factoryArg.getDescriptor() + ")V", false);
        createContainer.visitInsn(Opcodes.ARETURN);
        createContainer.visitEnd();
        containerFactoryWriter.visitEnd();
        @SuppressWarnings("unchecked") Class<? extends FeedbackContainerFactory<?, ?>> ret = (Class<? extends FeedbackContainerFactory<?, ?>>) generateClass(containerFactoryWriter, factoryImplName);
        return ret;
    }

    public static Class<?> generateClass(ClassNode classNode, String name) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return generateClass(writer, name);
    }

    public static Class<?> generateClass(ClassWriter classWriter, String name) {
        try {
            byte[] bytes = classWriter.toByteArray();
            new ClassReader(bytes).accept(new CheckClassAdapter(null), 0);
            Path path = Paths.get(name + ".class");
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
            return CcaClassLoader.INSTANCE.define(name.replace('/', '.'), bytes);
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            throw new StaticComponentLoadingException("Failed to generate class " + name, e);
        }
    }
}
