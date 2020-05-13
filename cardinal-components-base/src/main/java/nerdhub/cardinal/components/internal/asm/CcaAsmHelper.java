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
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class CcaAsmHelper {
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

    private static final Map<Type, TypeData> typeCache = new HashMap<>();

    private static TypeData getTypeData(Type type) throws IOException {
        TypeData t = typeCache.get(type);
        if (t != null) {
            return t;
        }
        String className = type.getInternalName();
        byte[] rawClass = FabricLauncherBase.getLauncher().getClassByteArray(className.replace('.', '/'));
        if (rawClass == null) throw new NoSuchFileException(className);
        ClassReader reader = new ClassReader(rawClass);
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
        TypeData tSuperData = getTypeData(tSuper);
        TypeData tSubData = getTypeData(tSub);
        if (Modifier.isInterface(tSuperData.getReader().getAccess())) {
            for (String itf : tSubData.getReader().getInterfaces()) {
                if (isAssignableFrom(tSuper, Type.getObjectType(itf))) {
                    return true;
                }
            }
        }
        return isAssignableFrom(tSuper, tSubData.getSupertype());
    }

    public static ClassReader getClassReader(Type type) throws IOException {
        return getTypeData(type).getReader();
    }

    public static ClassNode getClassNode(Type type) throws IOException {
        return getTypeData(type).getNode();
    }

    public static Class<?> generateClass(ClassNode classNode, String name) throws IOException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return generateClass(writer, name);
    }

    private static Class<?> generateClass(ClassWriter classWriter, String name) throws IOException {
        try {
            byte[] bytes = classWriter.toByteArray();
            if (DEBUG_CLASSES) {
                new ClassReader(bytes).accept(new CheckClassAdapter(null), 0);
                Path path = Paths.get(name + ".class");
                Files.createDirectories(path.getParent());
                Files.write(path, bytes);
            }
            return CcaClassLoader.INSTANCE.define(name.replace('/', '.'), bytes);
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            // IllegalStateException and IllegalArgumentException can be thrown by CheckClassAdapter
            throw new IOException("Failed to generate class " + name, e);
        }
    }

    public static void clearCache() {
        typeCache.clear();
    }

    public static String getComponentTypeName(String identifier) {
        return STATIC_COMPONENT_TYPE + "$" + getJavaIdentifierName(identifier);
    }

    @Nonnull
    public static String getJavaIdentifierName(String identifier) {
        return identifier.replace(':', '$');
    }

    @Nonnull
    public static String getTypeConstantName(String identifier) {
        return getJavaIdentifierName(identifier).toUpperCase(Locale.ROOT);
    }

    @Nonnull
    public static String getStaticStorageGetterName(String identifier) {
        return "get$" + getJavaIdentifierName(identifier);
    }
}
