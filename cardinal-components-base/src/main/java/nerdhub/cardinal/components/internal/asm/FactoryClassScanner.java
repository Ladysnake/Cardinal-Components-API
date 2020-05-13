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

import nerdhub.cardinal.components.internal.StaticComponentPlugin;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Scans classes to find factory methods
 */
public final class FactoryClassScanner extends ClassVisitor {

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
        return new FactoryMethodScanner(this.api, node, access, new MethodData(this.factoryOwnerType, node));
    }

    public static final class AsmFactoryData {
        final AnnotationNode annotation;
        final StaticComponentPlugin plugin;
        final MethodData factory;

        public AsmFactoryData(AnnotationNode annotation, StaticComponentPlugin plugin, MethodData factory) {
            this.annotation = annotation;
            this.plugin = plugin;
            this.factory = factory;
        }

        public MethodData getFactoryDescriptor() {
            return this.factory;
        }
    }

    /**
     * A method visitor that replaces all references to the old superclass
     */
    private class FactoryMethodScanner extends MethodVisitor {
        private final int access;
        private final MethodData factoryDescriptor;
        private List<AsmFactoryData> factoryData;

        public FactoryMethodScanner(int api, @Nullable MethodVisitor mv, int access, MethodData descriptor) {
            super(api, mv);
            this.access = access;
            this.factoryDescriptor = descriptor;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            AnnotationNode ret = (AnnotationNode) super.visitAnnotation(descriptor, visible);
            StaticComponentPlugin plugin = FactoryClassScanner.this.staticProviderAnnotations.get(descriptor);
            if (plugin != null) {
                if ((this.access & Opcodes.ACC_STATIC) == 0) {
                    throw new StaticComponentLoadingException("Factory method " + this.factoryDescriptor + " annotated with " + descriptor + " must be static");
                }
                try {
                    if (!CcaAsmHelper.isAssignableFrom(Type.getObjectType(CcaAsmHelper.COMPONENT), this.factoryDescriptor.descriptor.getReturnType())) {
                        throw new StaticComponentLoadingException("Factory method " + this.factoryDescriptor + " must return " + CcaAsmHelper.COMPONENT.replace('/', '.') + " or a subclass.");
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                if (this.factoryData == null) this.factoryData = new ArrayList<>();
                AsmFactoryData data = new AsmFactoryData(ret, plugin, this.factoryDescriptor);
                this.factoryData.add(data);
            }
            return ret;
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            if (this.factoryData != null) {
                for (AsmFactoryData data : this.factoryData) {
                    try {
                        String scanned = data.plugin.scan(data.getFactoryDescriptor(), AnnotationData.create(data.annotation));
                        if (!StaticComponentPlugin.IDENTIFIER_PATTERN.matcher(scanned).matches()) throw new StaticComponentLoadingException(scanned + "(returned by " + data.plugin.getClass().getTypeName() + "#scan) is not a valid identifier");
                        FactoryClassScanner.this.staticComponentTypes.add(scanned);
                    } catch (IOException e) {
                        throw new StaticComponentLoadingException("Failed to gather static component information from " + data.getFactoryDescriptor(), e);
                    }
                }
            }
        }
    }
}
