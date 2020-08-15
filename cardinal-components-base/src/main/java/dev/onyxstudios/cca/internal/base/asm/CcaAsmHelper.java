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

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.internal.base.ComponentRegistryImpl;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.util.container.FastComponentContainer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class CcaAsmHelper {

    /**
     * If {@code true}, any class generated through {@link #generateClass(ClassWriter, String)} will
     * be checked and written to disk. Highly recommended when editing methods in this class.
     */
    public static final boolean DEBUG_CLASSES = Boolean.getBoolean("cca.debug.asm");
    public static final int ASM_VERSION = Opcodes.ASM6;
    // existing references
    public static final String COMPONENT = Type.getInternalName(Component.class);
    public static final String COMPONENT_CONTAINER = Type.getInternalName(ComponentContainer.class);
    public static final String COMPONENT_TYPE = Type.getInternalName(ComponentType.class);
    public static final String COMPONENT_PROVIDER = Type.getInternalName(ComponentProvider.class);
    public static final String DYNAMIC_COMPONENT_CONTAINER_IMPL = Type.getInternalName(FastComponentContainer.class);
    public static final String IDENTIFIER = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_2960").replace('.', '/');
    public static final String EVENT = Type.getInternalName(Event.class);
    // methods
    public static final String BY_RAW_ID_DESC;
    public static final String BY_RAW_ID;
    public static final String COMPONENT_CONTAINER$GET_DESC;
    // generated references
    public static final String STATIC_COMPONENT_CONTAINER = "dev/onyxstudios/cca/_generated_/GeneratedComponentContainer";
    public static final String STATIC_CONTAINER_GETTER_DESC = "()Lnerdhub/cardinal/components/api/component/Component;";
    public static final String STATIC_COMPONENT_TYPE = "dev/onyxstudios/cca/_generated_/ComponentType";
    public static final String STATIC_CONTAINER_FACTORY = "dev/onyxstudios/cca/_generated_/GeneratedContainerFactory";

    static {
        try {
            Method byRawId = ComponentRegistryImpl.class.getMethod("byRawId", int.class);
            BY_RAW_ID_DESC = Type.getMethodDescriptor(byRawId);
            BY_RAW_ID = byRawId.getName();
            COMPONENT_CONTAINER$GET_DESC = Type.getMethodDescriptor(nerdhub.cardinal.components.api.component.ComponentContainer.class.getMethod("get", ComponentType.class));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to find one or more method descriptors", e);
        }
    }

    public static Class<?> generateClass(ClassNode classNode) throws IOException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return generateClass(writer, classNode.name);
    }

    private static Class<?> generateClass(ClassWriter classWriter, String className) throws IOException {
        try {
            byte[] bytes = classWriter.toByteArray();
            if (DEBUG_CLASSES) {
                ClassReader classReader = new ClassReader(bytes);
                classReader.accept(new CheckClassAdapter(null), 0);
                Path path = Paths.get(classReader.getClassName() + ".class");
                Files.createDirectories(path.getParent());
                Files.write(path, bytes);
            }
            return CcaClassLoader.INSTANCE.define(className.replace('/', '.'), bytes);
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            // IllegalStateException and IllegalArgumentException can be thrown by CheckClassAdapter
            throw new IOException("Failed to generate class " + className, e);
        }
    }

    public static String getComponentTypeName(Identifier identifier) {
        return STATIC_COMPONENT_TYPE + "$" + getJavaIdentifierName(identifier);
    }

    public static String getJavaIdentifierName(Identifier identifier) {
        return identifier.toString()
            .replace(':', '$')
            .replace('/', '$')
            .replace('.', '\u00A4' /*¤*/)
            .replace('-', '\u00A3' /*£*/);
    }

    public static String getStaticStorageGetterName(Identifier identifier) {
        return "get$" + getJavaIdentifierName(identifier);
    }

    public static Method findSam(Class<?> callbackClass) {
        if (!callbackClass.isInterface()) {
            throw badFunctionalInterface(callbackClass);
        }
        Method ret = null;
        for (Method m : callbackClass.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                if (ret != null) {
                    throw badFunctionalInterface(callbackClass);
                }
                ret = m;
            }
        }
        if (ret == null) {
            throw badFunctionalInterface(callbackClass);
        }
        return ret;
    }

    private static IllegalArgumentException badFunctionalInterface(Class<?> callbackClass) {
        return new IllegalArgumentException(callbackClass + " is not a functional interface!");
    }

    public static void stackStaticComponentType(MethodVisitor method, Identifier componentId) {
        method.visitLdcInsn(((ComponentRegistryImpl) ComponentRegistry.INSTANCE).assignRawId(componentId));
        // stack: rawId
        method.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ComponentRegistryImpl.class), BY_RAW_ID, BY_RAW_ID_DESC, false);
        // stack: componentType
    }
}
