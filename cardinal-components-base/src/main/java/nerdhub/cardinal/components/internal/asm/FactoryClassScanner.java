package nerdhub.cardinal.components.internal.asm;

import nerdhub.cardinal.components.internal.StaticComponentPlugin;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Scans classes to find factory methods
 */
public class FactoryClassScanner extends ClassVisitor {

    private final Map<String, StaticComponentPlugin> staticProviderAnnotations;
    private final Set<String> staticComponentTypes;
    private Type factoryOwnerType;

    public FactoryClassScanner(int api, ClassVisitor cv, Map<String, StaticComponentPlugin> staticProviderAnnotations, Set<String> staticComponentTypes) {
        super(api, cv);
        this.staticProviderAnnotations = staticProviderAnnotations;
        this.staticComponentTypes = staticComponentTypes;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.factoryOwnerType = Type.getObjectType(name);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodNode node = new MethodNode(access, name, desc, signature, exceptions);
        return new FactoryMethodScanner(this.api, node, access, new NamedMethodDescriptor(this.factoryOwnerType, name, Type.getMethodType(desc)));
    }

    public static class AsmFactoryData {
        final Map<String, Object> annotationData = new HashMap<>();
        final StaticComponentPlugin plugin;
        final NamedMethodDescriptor factory;

        public AsmFactoryData(StaticComponentPlugin plugin, NamedMethodDescriptor factory) {
            this.plugin = plugin;
            this.factory = factory;
        }

        public NamedMethodDescriptor getFactoryDescriptor() {
            return factory;
        }

        /**
         * @param valueName the value name
         * @return the actual value, whose type must be {@link Byte}, {@link Boolean}, {@link
         * Character}, {@link Short}, {@link Integer} , {@link Long}, {@link Float}, {@link Double},
         * {@link String} or {@link Type} of {@link Type#OBJECT} or {@link Type#ARRAY} sort. This
         * value can also be an array of byte, boolean, short, char, int, long, float or double values.
         */
        public Object get(String valueName) {
            Object value = annotationData.get(valueName);
            if (value == null) throw new NoSuchElementException("Unrecognized annotation key " + valueName);
            return value;
        }
    }

    /**
     * A method visitor that replaces all references to the old superclass
     */
    private class FactoryMethodScanner extends MethodVisitor {
        private final int access;
        private final NamedMethodDescriptor factoryDescriptor;
        private List<AsmFactoryData> factoryData;

        public FactoryMethodScanner(int api, @Nullable MethodVisitor mv, int access, NamedMethodDescriptor descriptor) {
            super(api, mv);
            this.access = access;
            this.factoryDescriptor = descriptor;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            AnnotationVisitor base = super.visitAnnotation(descriptor, visible);
            StaticComponentPlugin plugin = staticProviderAnnotations.get(descriptor);
            if (plugin != null) {
                if ((access & Opcodes.ACC_STATIC) == 0) {
                    throw new StaticComponentLoadingException("Factory method " + factoryDescriptor + " annotated with " + descriptor + " must be static");
                }
                if (factoryDescriptor.descriptor.getReturnType().getDescriptor().equals(CcaAsmConstants.COMPONENT)) {
                    throw new StaticComponentLoadingException("Factory method " + factoryDescriptor + " must return " + CcaAsmConstants.COMPONENT.replace('/', '.'));
                }
                if (factoryData == null) factoryData = new ArrayList<>();
                AsmFactoryData data = new AsmFactoryData(plugin, this.factoryDescriptor);
                factoryData.add(data);
                return new FactoryMethodScanner.FactoryAnnotationScanner(this.api, base, data);
            }
            return base;
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            if (this.factoryData != null) {
                for (AsmFactoryData data : this.factoryData) {
                    staticComponentTypes.add(data.plugin.scan(data, (MethodNode) this.mv));
                }
            }
        }

        private class FactoryAnnotationScanner extends AnnotationVisitor {
            private final AsmFactoryData data;

            public FactoryAnnotationScanner(int api, @Nullable AnnotationVisitor annotationVisitor, AsmFactoryData data) {
                super(api, annotationVisitor);
                this.data = data;
            }

            @Override
            public void visit(String name, Object value) {
                super.visit(name, value);
                this.data.annotationData.put(name, value);
            }
        }
    }
}
