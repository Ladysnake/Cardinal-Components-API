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

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.asm.AnnotationData;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.NamedMethodDescriptor;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class SimpleStaticComponentPlugin implements StaticComponentPlugin {
    private final Map<String, NamedMethodDescriptor> componentFactories = new HashMap<>();
    private final String providerClass;
    private final String implSuffix;
    private final Class<? extends Annotation> annotationType;
    private Class<? extends FeedbackContainerFactory<?, ?>> factoryClass;

    protected SimpleStaticComponentPlugin(String className, String implSuffix, Class<? extends Annotation> annotationType) {
        this.providerClass = className;
        this.implSuffix = implSuffix;
        this.annotationType = annotationType;
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
    public String scan(NamedMethodDescriptor factoryDescriptor, AnnotationData data, MethodNode method) {
        if (factoryDescriptor.descriptor.getArgumentTypes().length > 1) {
            throw new StaticComponentLoadingException("Too many arguments in method " + factoryDescriptor + ". Should be either no-args or a single " + this.providerClass + " argument.");
        }
        String value = data.get("value", String.class);
        this.componentFactories.put(value, factoryDescriptor);
        return value;
    }

    @ApiStatus.OverrideOnly
    @Override
    public void generate() {
        Type levelType = Type.getObjectType(this.providerClass.replace('.', '/'));
        Class<? extends ComponentContainer<?>> containerCls = CcaAsmHelper.defineContainer(this.componentFactories, this.implSuffix, levelType);
        this.factoryClass = CcaAsmHelper.defineSingleArgFactory(this.implSuffix, Type.getType(containerCls), levelType);
    }
}
