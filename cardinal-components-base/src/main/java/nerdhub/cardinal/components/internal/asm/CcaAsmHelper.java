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

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Locale;

public final class CcaAsmHelper {
    private static final sun.misc.Unsafe UNSAFE;

    /**
     * If {@code true}, any class generated through {@link #generateClass(ClassWriter, String)} will
     * be checked and written to disk. Highly recommended when editing methods in this class.
     */
    public static final boolean DEBUG_CLASSES = Boolean.getBoolean("cca.debug.asm");
    public static final int ASM_VERSION = Opcodes.ASM6;
    // existing references
    public static final String COMPONENT = "nerdhub/cardinal/components/api/component/Component";
    public static final String COMPONENT_CONTAINER = "nerdhub/cardinal/components/api/component/ComponentContainer";
    public static final String COMPONENT_TYPE = "nerdhub/cardinal/components/api/ComponentType";
    public static final String COMPONENT_PROVIDER = "nerdhub/cardinal/components/api/component/ComponentProvider";
    public static final String CONTAINER_FACTORY_IMPL = "nerdhub/cardinal/components/internal/FeedbackContainerFactory";
    public static final String DYNAMIC_COMPONENT_CONTAINER_IMPL = "nerdhub/cardinal/components/api/util/container/FastComponentContainer";
    public static final String LAZY_COMPONENT_TYPE = "nerdhub/cardinal/components/api/util/LazyComponentType";
    public static final String IDENTIFIER = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_2960").replace('.', '/');
    public static final String EVENT = "net/fabricmc/fabric/api/event/Event";
    // generated references
    public static final String STATIC_COMPONENT_CONTAINER = "nerdhub/cardinal/components/_generated_/GeneratedComponentContainer";
    public static final String STATIC_CONTAINER_GETTER_DESC = "()Lnerdhub/cardinal/components/api/component/Component;";
    public static final String STATIC_COMPONENT_TYPE = "nerdhub/cardinal/components/_generated_/ComponentType";
    public static final String STATIC_COMPONENT_TYPES = "nerdhub/cardinal/components/_generated_/StaticComponentTypes";
    public static final String STATIC_CONTAINER_FACTORY = "nerdhub/cardinal/components/_generated_/GeneratedContainerFactory";

    private static final ClassLoader CLASSLOADER = CcaAsmHelper.class.getClassLoader();
    private static final ProtectionDomain PROTECTION_DOMAIN = CcaAsmHelper.class.getProtectionDomain();

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
            return UNSAFE.defineClass(className.replace('/', '.'), bytes, 0, bytes.length, CLASSLOADER, PROTECTION_DOMAIN);
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            // IllegalStateException and IllegalArgumentException can be thrown by CheckClassAdapter
            throw new IOException("Failed to generate class " + className, e);
        }
    }

    public static String getComponentTypeName(Identifier identifier) {
        return STATIC_COMPONENT_TYPE + "$" + getJavaIdentifierName(identifier);
    }

    @Nonnull
    public static String getJavaIdentifierName(Identifier identifier) {
        return identifier.toString().replace(':', '$').replace('/', '$');
    }

    @Nonnull
    public static String getTypeConstantName(Identifier identifier) {
        return getJavaIdentifierName(identifier).toUpperCase(Locale.ROOT);
    }

    @Nonnull
    public static String getStaticStorageGetterName(Identifier identifier) {
        return "get$" + getJavaIdentifierName(identifier);
    }

    static {
        try {
            Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (sun.misc.Unsafe) theUnsafe.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new StaticComponentLoadingException("Failed to retrieve Unsafe", e);
        }
    }

    public static Method findSam(Class<?> callbackClass) {
        Method ret = null;
        for (Method m : callbackClass.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                if (ret != null) {
                    throw new IllegalArgumentException(callbackClass + " is not a functional interface!");
                }
                ret = m;
            }
        }
        if (ret == null) {
            throw new IllegalArgumentException(callbackClass + " is not a functional interface!");
        }
        return ret;
    }
}
