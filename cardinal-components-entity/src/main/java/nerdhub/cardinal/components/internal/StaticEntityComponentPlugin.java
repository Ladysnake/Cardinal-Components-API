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

import nerdhub.cardinal.components.api.EntityComponentFactory;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.FactoryClassScanner;
import nerdhub.cardinal.components.internal.asm.NamedMethodDescriptor;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class StaticEntityComponentPlugin implements StaticComponentPlugin {
    public static final StaticEntityComponentPlugin INSTANCE = new StaticEntityComponentPlugin();

    private static String getSuffix(Type entityClass) {
        String internalName = entityClass.getInternalName();
        String simpleName = internalName.substring(internalName.lastIndexOf('/') + 1);
        return String.format("EntityImpl_%s_%d", simpleName, Integer.toUnsignedLong(internalName.hashCode()));
    }

    private final Map<Type, Map</*Identifier*/String, NamedMethodDescriptor>> componentFactories = new HashMap<>();
    private final String entityClass = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_1297");
    private final Map<Type, Class<? extends FeedbackContainerFactory<?, ?>>> factoryClasses = new HashMap<>();

    public Class<? extends FeedbackContainerFactory<?, ?>> getFactoryClass(Class<? extends Entity> entityClass) {
        return factoryClasses.get(Type.getType(entityClass));
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return EntityComponentFactory.class;
    }

    @Override
    public String scan(FactoryClassScanner.AsmFactoryData data, MethodNode method) throws IOException {
        NamedMethodDescriptor factoryDescriptor = data.getFactoryDescriptor();
        Type[] factoryArgs = factoryDescriptor.args;
        if (factoryArgs.length > 1) {
            throw new StaticComponentLoadingException("Too many arguments in method " + factoryDescriptor + ". Should be either no-args or a single " + entityClass + " argument.");
        }
        Type targets;
        Type annotationTargets = (Type) data.getOrNull("targets");
        if (annotationTargets != null) {
            if (factoryArgs.length != 0 && !CcaAsmHelper.isAssignableFrom(factoryArgs[0], annotationTargets)) {
                throw new IllegalStateException("Argument " + factoryArgs[0] + " in method " + factoryDescriptor + " is not assignable from declared target entity class " + annotationTargets);
            }
            targets = annotationTargets;
        } else {
            if (factoryArgs.length == 0) {
                throw new StaticComponentLoadingException("Cannot determine target entity class in method '" + factoryDescriptor + "'. Either specify an entity parameter of the target class, or explicitly specify the EntityComponentFactory#targets property.");
            } else {
                targets = factoryArgs[0];
            }
        }
        String value = (String) data.get("value");
        componentFactories.computeIfAbsent(targets, t -> new HashMap<>()).put(value, factoryDescriptor);
        return value;
    }

    @Override
    public void generate() throws IOException {
        Type entityType = Type.getObjectType(entityClass.replace('.', '/'));
        for (Map.Entry<Type, Map<String, NamedMethodDescriptor>> entry : this.componentFactories.entrySet()) {
            Map<String, NamedMethodDescriptor> compiled = new HashMap<>(entry.getValue());
            Type type = entry.getKey();
            while (!type.equals(entityType)) {
                type = CcaAsmHelper.getSuperclass(type);
                this.componentFactories.getOrDefault(type, Collections.emptyMap()).forEach(compiled::putIfAbsent);
            }
            String implSuffix = getSuffix(entry.getKey());
            Class<? extends ComponentContainer<?>> containerCls = CcaAsmHelper.defineContainer(compiled, implSuffix, entityType);
            this.factoryClasses.put(entry.getKey(), CcaAsmHelper.defineSingleArgFactory(implSuffix, Type.getType(containerCls), entityType));
        }
    }
}
