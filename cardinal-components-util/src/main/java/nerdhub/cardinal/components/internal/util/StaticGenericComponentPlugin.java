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
package nerdhub.cardinal.components.internal.util;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.GenericComponentFactory;
import nerdhub.cardinal.components.internal.FeedbackContainerFactory;
import nerdhub.cardinal.components.internal.StaticComponentPlugin;
import nerdhub.cardinal.components.internal.StaticComponentPluginBase;
import nerdhub.cardinal.components.internal.asm.AnnotationData;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.MethodData;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public final class StaticGenericComponentPlugin implements StaticComponentPlugin {
    public static final StaticGenericComponentPlugin INSTANCE = new StaticGenericComponentPlugin();

    private static String getSuffix(String itemId) {
        return "GenericImpl_" + itemId.replace(':', '$').replace('/', '$');
    }

    private final Map</*Identifier*/String, Map</*ComponentType*/String, MethodData>> componentFactories = new HashMap<>();
    private final Set<String> claimedFactories = new LinkedHashSet<>();

    Class<? extends ComponentContainer<?>> spinComponentContainer(String genericTypeId, Class<? extends Component> expectedComponentClass, Class<?>... argClasses) throws IOException {
        Type rType = Type.getType(expectedComponentClass);
        Type[] args = new Type[argClasses.length];
        for (int i = 0; i < argClasses.length; i++) {
            args[i] = Type.getType(argClasses[i]);
        }
        if (this.claimedFactories.contains(genericTypeId)) {
            throw new IllegalStateException("A component container factory for " + genericTypeId + " already exists.");
        }
        Map<String, MethodData> componentFactories = this.componentFactories.getOrDefault(genericTypeId, Collections.emptyMap());
        for (MethodData factory : componentFactories.values()) {
            if (!CcaAsmHelper.isAssignableFrom(rType, factory.descriptor.getReturnType())) {
                throw new StaticComponentLoadingException("Bad return type in component factory " + factory + ", expected " + expectedComponentClass + " or a subclass");
            }
            Type[] factoryArgs = factory.descriptor.getArgumentTypes();
            if (factoryArgs.length > args.length) {
                throw new StaticComponentLoadingException(
                    String.format("Too many arguments in method %s. Should be at most %d arguments with types %s.", factory, factoryArgs.length, Arrays.stream(factoryArgs).map(Type::getClassName).collect(Collectors.joining(", ", "[", "]")))
                );
            }
            for (int i = 0; i < factoryArgs.length; i++) {
                if (!Objects.equals(factoryArgs[i], args[i])) {
                    throw new StaticComponentLoadingException("Invalid argument " + args[i].getClassName().replaceAll(".*\\.", "") + " in component factory " + factory + ", expected " + args[i].getClassName().replaceAll(".*\\.", ""));
                }
            }
        }
        Class<? extends ComponentContainer<?>> containerClass = StaticComponentPluginBase.spinComponentContainer(componentFactories, getSuffix(genericTypeId), args);
        this.claimedFactories.add(genericTypeId);
        return containerClass;
    }

    Class<? extends FeedbackContainerFactory<?, ?>> spinSingleArgContainerFactory(String genericTypeId, Class<? extends Component> componentClass, Class<?> argClass) throws IOException {
        Class<? extends ComponentContainer<?>> containerClass = this.spinComponentContainer(genericTypeId, componentClass, argClass);
        return StaticComponentPluginBase.spinSingleArgFactory(getSuffix(genericTypeId), Type.getType(containerClass), Type.getType(argClass));
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return GenericComponentFactory.class;
    }

    @Override
    public String scan(MethodData factory, AnnotationData annotation) {
        List<String> targets = annotation.get("targets", List.class);
        Set<String> resolvedTargets = new HashSet<>(targets);
        if (targets.size() != resolvedTargets.size()) {
            throw new StaticComponentLoadingException("Component factory '" + factory + "' is trying to subscribe with duplicate ids (" + String.join(", ", targets) + ")");
        }
        String value = annotation.get("value", String.class);
        for (String target : resolvedTargets) {
            if (!IDENTIFIER_PATTERN.matcher(target).matches()) {
                throw new StaticComponentLoadingException("Component factory '" + factory + "' is subscribing with invalid id: " + target);
            }
            Map<String, MethodData> specializedMap = this.componentFactories.computeIfAbsent(target, t -> new HashMap<>());
            MethodData previousFactory = specializedMap.get(value);
            if (previousFactory != null) {
                throw new StaticComponentLoadingException("Duplicate factory declarations for " + value + " on id '" + target + "': " + factory + " and " + previousFactory);
            }
            specializedMap.put(value, factory);
        }
        return value;
    }

    @Override
    public void generate() {
        // NO-OP, generation is done on demand
    }
}
