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

import dev.onyxstudios.cca.api.v3.component.*;
import dev.onyxstudios.cca.internal.base.ComponentRegistryImpl;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public static final String FAST_COMPONENT_CONTAINER_CTOR_DESC;
    public static final String CAN_BE_ASSIGNED_DESC;

    static {
        try {
            Method byRawId = ComponentRegistryImpl.class.getMethod("byRawId", int.class);
            BY_RAW_ID_DESC = Type.getMethodDescriptor(byRawId);
            BY_RAW_ID = byRawId.getName();
            COMPONENT_CONTAINER$GET_DESC = Type.getMethodDescriptor(nerdhub.cardinal.components.api.component.ComponentContainer.class.getMethod("get", ComponentType.class));
            FAST_COMPONENT_CONTAINER_CTOR_DESC = Type.getConstructorDescriptor(FastComponentContainer.class.getConstructor(int.class));
            CAN_BE_ASSIGNED_DESC = Type.getMethodDescriptor(FastComponentContainer.class.getDeclaredMethod("canBeAssigned", ComponentType.class));
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

    /**
     * Defines an implementation of {@link ComponentContainer} that supports direct component access.
     *
     * <p>Instances of the returned class can be returned by {@link ComponentProvider#getComponentContainer()}.
     * <strong>This method must not be called before the static component container interface has been defined!</strong>
     *
     * <p>Generated component container classes will take an additional {@code int} as first argument to their
     * constructors (until 3.0). That number corresponds to the expected dynamic size of the container (see {@link FastComponentContainer}).
     *
     * @param componentFactoryType the interface implemented by the component factories used to initialize this container
     * @param componentFactories   a map of {@link ComponentType} ids to factories for components of that type
     * @param implNameSuffix       a unique suffix for the generated class
     * @return the generated container class
     */
    public static <I> Class<? extends ComponentContainer> spinComponentContainer(Class<? super I> componentFactoryType, Map<ComponentKey<?>, I> componentFactories, String implNameSuffix) throws IOException {
        return spinComponentContainer(componentFactoryType, componentFactories, componentFactories.keySet().stream().collect(Collectors.toMap(Function.identity(), ComponentKey::getComponentClass)), implNameSuffix);
    }

    /**
     * Defines an implementation of {@link ComponentContainer} that supports direct component access.
     *
     * <p>Instances of the returned class can be returned by {@link ComponentProvider#getComponentContainer()}.
     * <strong>This method must not be called before the static component container interface has been defined!</strong>
     *
     * <p>Generated component container classes will take an additional {@code int} as first argument to their
     * constructors (until 3.0). That number corresponds to the expected dynamic size of the container (see {@link FastComponentContainer}).
     *
     * @param componentFactoryType the interface implemented by the component factories used to initialize this container
     * @param componentFactories   a map of {@link ComponentKey}s to factories for components of that type
     * @param componentImpls       a map of {@link ComponentKey}s to their actual implementation classes for the container
     * @param implNameSuffix       a unique suffix for the generated class
     * @return the generated container class
     */
    public static <I> Class<? extends ComponentContainer> spinComponentContainer(Class<? super I> componentFactoryType, Map<ComponentKey<?>, I> componentFactories, Map<ComponentKey<?>, Class<? extends Component>> componentImpls, String implNameSuffix) throws IOException {
        CcaBootstrap.INSTANCE.ensureInitialized();

        checkValidJavaIdentifier(implNameSuffix);
        String containerImplName = STATIC_COMPONENT_CONTAINER + '_' + implNameSuffix;
        String componentFactoryName = Type.getInternalName(componentFactoryType);
        Method sam = findSam(componentFactoryType);
        String samDescriptor = Type.getMethodDescriptor(sam);
        // TODO V3 remove
        Int2ObjectMap<String> componentFieldDescriptors = new Int2ObjectOpenHashMap<>();
        Class<?>[] factoryArgs = sam.getParameterTypes();
        Type[] actualCtorArgs = new Type[factoryArgs.length + 1];
        actualCtorArgs[0] = Type.INT_TYPE;

        for (int i = 0; i < factoryArgs.length; i++) {
            actualCtorArgs[i + 1] = Type.getType(factoryArgs[i]);
        }

        String ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, actualCtorArgs);
        ClassNode classNode = new ClassNode(ASM_VERSION);
        classNode.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
            containerImplName,
            null,
            STATIC_COMPONENT_CONTAINER,
            null
        );

        String factoryFieldDescriptor = Type.getDescriptor(componentFactoryType);

        classNode.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "componentKeys", "Ljava/util/Set;", null, null);
//      TODO V3 replace staticKeySet() with keys() when dynamic components are no more
        MethodVisitor keys = classNode.visitMethod(Opcodes.ACC_PUBLIC, "staticKeySet", "()Ljava/util/Set;", null, null);
        keys.visitFieldInsn(Opcodes.GETSTATIC, containerImplName, "componentKeys", "Ljava/util/Set;");
        keys.visitInsn(Opcodes.ARETURN);
        keys.visitEnd();

/*      TODO V3 enable empty check optimization when dynamic components are no more
        MethodVisitor hasComponents = classNode.visitMethod(Opcodes.ACC_PUBLIC, "hasComponents", "()Z", null, null);
        hasComponents.visitCode();
        hasComponents.visitLdcInsn(!componentFactories.isEmpty());
        hasComponents.visitInsn(Opcodes.IRETURN);
        hasComponents.visitEnd();
*/

        MethodVisitor init = classNode.visitMethod(Opcodes.ACC_PUBLIC, "<init>", ctorDesc, null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitVarInsn(Opcodes.ILOAD, 1);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, STATIC_COMPONENT_CONTAINER, "<init>", FAST_COMPONENT_CONTAINER_CTOR_DESC, false);

        MethodVisitor serverTick = classNode.visitMethod(Opcodes.ACC_PUBLIC, "tickServerComponents", "()V", null, null);
        serverTick.visitCode();
        MethodVisitor clientTick = classNode.visitMethod(Opcodes.ACC_PUBLIC, "tickClientComponents", "()V", null, null);
        clientTick.visitCode();

        for (ComponentKey<?> key : componentFactories.keySet()) {
            Identifier identifier = key.getId();
            String componentFieldName = getJavaIdentifierName(identifier);
            Class<? extends Component> impl = componentImpls.get(key);
            String componentFieldDescriptor = Type.getDescriptor(impl);
            componentFieldDescriptors.put(key.getRawId(), componentFieldDescriptor);
            String factoryFieldName = getFactoryFieldName(identifier);
            /* field declaration */
            classNode.visitField(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                factoryFieldName,
                factoryFieldDescriptor,
                null,
                null
            ).visitEnd();
            classNode.visitField(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                componentFieldName,
                componentFieldDescriptor,
                null,
                null
            ).visitEnd();
            /* constructor initialization */
            init.visitFieldInsn(Opcodes.GETSTATIC, containerImplName, factoryFieldName, factoryFieldDescriptor);
            // stack: factory
            for (int i = 0; i < factoryArgs.length; i++) {
                init.visitVarInsn(Opcodes.ALOAD, i + 2);    // first 2 args are for the container itself
            }
            // stack: factory factoryArgs...
            // initialize the component by calling the factory
            init.visitMethodInsn(Opcodes.INVOKEINTERFACE, componentFactoryName, sam.getName(), samDescriptor, true);
            // stack: component
            init.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Objects", "requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            // stack: object
            init.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(impl));
            // stack: component
            init.visitVarInsn(Opcodes.ALOAD, 0);
            // stack: component <this>
            init.visitInsn(Opcodes.SWAP);
            // stack: <this> component
            // store in the field
            init.visitFieldInsn(Opcodes.PUTFIELD, containerImplName, componentFieldName, componentFieldDescriptor);
            // <empty stack>
            init.visitVarInsn(Opcodes.ALOAD, 0);
            // stack: <this>
            stackStaticComponentType(init, identifier);
            // stack: <this> componentType
            init.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DYNAMIC_COMPONENT_CONTAINER_IMPL, "addContainedType", "(L" + COMPONENT_TYPE + ";)V", false);
            // <empty stack>

            /* getter implementation */
            MethodVisitor getter = classNode.visitMethod(
                Opcodes.ACC_PUBLIC,
                getStaticStorageGetterName(identifier),
                STATIC_CONTAINER_GETTER_DESC,
                null,
                null
            );
            getter.visitVarInsn(Opcodes.ALOAD, 0);
            // stack: <this>
            getter.visitFieldInsn(Opcodes.GETFIELD, containerImplName, componentFieldName, componentFieldDescriptor);
            // stack: component
            getter.visitInsn(Opcodes.ARETURN);
            getter.visitEnd();

            /* tick implementation */
            if (ServerTickingComponent.class.isAssignableFrom(impl)) {
                generateTickImpl(containerImplName, serverTick, componentFieldName, impl, componentFieldDescriptor, "serverTick");
            }
            if (ClientTickingComponent.class.isAssignableFrom(impl)) {
                generateTickImpl(containerImplName, clientTick, componentFieldName, impl, componentFieldDescriptor, "clientTick");
            }
        }
        init.visitInsn(Opcodes.RETURN);
        init.visitEnd();
        serverTick.visitInsn(Opcodes.RETURN);
        serverTick.visitEnd();
        clientTick.visitInsn(Opcodes.RETURN);
        clientTick.visitEnd();

        if (!componentFactories.isEmpty()) {
            generateLookupMethods(componentFactories.keySet(), containerImplName, classNode, componentFieldDescriptors);
        }

        Class<? extends ComponentContainer> ret = generateClass(classNode).asSubclass(ComponentContainer.class);

        try {
            Field keySet = ret.getDeclaredField("componentKeys");
            keySet.setAccessible(true);
            // TODO use a custom class with baked in immutability + BitSet contains + array iterator
            keySet.set(null, Collections.unmodifiableSet(new ReferenceArraySet<>(componentFactories.keySet())));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new StaticComponentLoadingException("Failed to initialize the set of component keys for " + ret, e);
        }

        for (Map.Entry<ComponentKey<?>, I> entry : componentFactories.entrySet()) {
            try {
                Field factoryField = ret.getDeclaredField(getFactoryFieldName(entry.getKey().getId()));
                factoryField.setAccessible(true);
                factoryField.set(null, entry.getValue());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new StaticComponentLoadingException("Failed to initialize factory field for component type " + entry.getKey(), e);
            }
        }
        return ret;
    }

    private static void generateTickImpl(String containerImplName, MethodVisitor tick, String componentFieldName, Class<? extends Component> impl, String componentFieldDescriptor, String target) {
        tick.visitVarInsn(Opcodes.ALOAD, 0);
        // stack: <this>
        tick.visitFieldInsn(Opcodes.GETFIELD, containerImplName, componentFieldName, componentFieldDescriptor);
        // stack: component
        if (impl.isInterface()) {
            tick.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(impl), target, "()V", true);
        } else {
            tick.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(impl), target, "()V", false);
        }
    }

    // TODO V3 remove when dynamic components are gone
    private static void generateLookupMethods(Set<ComponentKey<?>> components, String containerImplName, ClassNode classNode, Int2ObjectMap<String> componentFieldDescriptors) {
        MethodVisitor canBeAssigned = classNode.visitMethod(Opcodes.ACC_PROTECTED, "canBeAssigned", CAN_BE_ASSIGNED_DESC, null, null);
        MethodVisitor get = classNode.visitMethod(Opcodes.ACC_PUBLIC, "get", COMPONENT_CONTAINER$GET_DESC, null, null);
        canBeAssigned.visitVarInsn(Opcodes.ALOAD, 1);
        // stack[canBeAssigned]: componentType
        get.visitVarInsn(Opcodes.ALOAD, 0);
        get.visitVarInsn(Opcodes.ALOAD, 1);
        // stack[get]: <this> componentType
        canBeAssigned.visitMethodInsn(Opcodes.INVOKEVIRTUAL, COMPONENT_TYPE, "getRawId", "()I", false);
        // stack[canBeAssigned]: rawId
        get.visitMethodInsn(Opcodes.INVOKEVIRTUAL, COMPONENT_TYPE, "getRawId", "()I", false);
        // stack[get]: <this> rawId
        Label yesAssign = new Label();
        Label noAssign = new Label();
        Label defaultGetCase = new Label();
        Int2ObjectSortedMap<Identifier> raw2Id = components.stream().collect(Collectors.toMap(
            ComponentKey::getRawId,
            ComponentKey::getId, (r, r2) -> {
                throw new IllegalStateException("Duplicate key " + r + ", " + r2);
            }, Int2ObjectRBTreeMap::new
        ));
        int nbCases = raw2Id.lastIntKey() + 1;  // 0 is a valid raw id
        Label[] getLabels = new Label[nbCases];
        Label[] canBeAssignedLabels = new Label[nbCases];
        for (int i = 0; i < nbCases; i++) {
            boolean contained = raw2Id.containsKey(i);
            getLabels[i] = contained ? new Label() : defaultGetCase;
            canBeAssignedLabels[i] = contained ? noAssign : yesAssign;
        }
        canBeAssigned.visitTableSwitchInsn(0, nbCases - 1, yesAssign, canBeAssignedLabels);
        // <empty stack[canBeAssigned]>
        get.visitTableSwitchInsn(0, nbCases - 1, defaultGetCase, getLabels);
        // stack[get]: <this>

        canBeAssigned.visitLabel(noAssign);
        canBeAssigned.visitInsn(Opcodes.ICONST_0);
        // stack[canBeAssigned]: <false>
        canBeAssigned.visitInsn(Opcodes.IRETURN);

        for (Int2ObjectMap.Entry<Identifier> entry : raw2Id.int2ObjectEntrySet()) {
            // stack[get]: <this>
            get.visitLabel(getLabels[entry.getIntKey()]);
            get.visitFieldInsn(Opcodes.GETFIELD, containerImplName, getJavaIdentifierName(entry.getValue()), componentFieldDescriptors.get(entry.getIntKey()));
            // stack[get]: component
            get.visitInsn(Opcodes.ARETURN);
        }
        canBeAssigned.visitLabel(yesAssign);
        // <empty stack[canBeAssigned]>
        canBeAssigned.visitVarInsn(Opcodes.ALOAD, 0);
        canBeAssigned.visitVarInsn(Opcodes.ALOAD, 1);
        canBeAssigned.visitMethodInsn(Opcodes.INVOKESPECIAL, DYNAMIC_COMPONENT_CONTAINER_IMPL, "canBeAssigned", CAN_BE_ASSIGNED_DESC, false);
        canBeAssigned.visitInsn(Opcodes.IRETURN);
        get.visitLabel(defaultGetCase);
        // stack[get]: <this>
        get.visitVarInsn(Opcodes.ALOAD, 1);
        // stack[get]: <this> componentType
        get.visitMethodInsn(Opcodes.INVOKESPECIAL, DYNAMIC_COMPONENT_CONTAINER_IMPL, "get", COMPONENT_CONTAINER$GET_DESC, false);
        get.visitInsn(Opcodes.ARETURN);
        get.visitEnd();
    }

    private static String getFactoryFieldName(Identifier identifier) {
        return getJavaIdentifierName(identifier) + "$factory";
    }

    public static void checkValidJavaIdentifier(String implNameSuffix) {
        for (int i = 0; i < implNameSuffix.length(); i++) {
            if (!Character.isJavaIdentifierPart(implNameSuffix.charAt(i))) {
                throw new IllegalArgumentException(implNameSuffix + " is not a valid suffix for a java identifier");
            }
        }
    }
}
