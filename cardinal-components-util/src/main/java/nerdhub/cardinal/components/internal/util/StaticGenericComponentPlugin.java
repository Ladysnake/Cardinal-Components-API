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
import nerdhub.cardinal.components.api.component.GenericComponentFactoryRegistry;
import nerdhub.cardinal.components.api.component.StaticGenericComponentInitializer;
import nerdhub.cardinal.components.internal.CcaBootstrap;
import nerdhub.cardinal.components.internal.DispatchingLazy;
import nerdhub.cardinal.components.internal.FeedbackContainerFactory;
import nerdhub.cardinal.components.internal.StaticComponentPluginBase;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.MethodData;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.minecraft.util.Identifier;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.stream.Collectors;

public final class StaticGenericComponentPlugin extends DispatchingLazy implements GenericComponentFactoryRegistry {
    public static final StaticGenericComponentPlugin INSTANCE = new StaticGenericComponentPlugin();
    private final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    private static String getSuffix(Identifier itemId) {
        return "GenericImpl_" + CcaAsmHelper.getJavaIdentifierName(itemId);
    }

    private final Map<Identifier, Map</*ComponentType*/Identifier, MethodHandleInfo>> componentFactories = new HashMap<>();
    private final Set<Identifier> claimedFactories = new LinkedHashSet<>();

    Class<? extends ComponentContainer<?>> spinComponentContainer(Identifier genericTypeId, Class<? extends Component> expectedComponentClass, Class<?>... argClasses) throws IOException {
        this.ensureInitialized();

        if (this.claimedFactories.contains(genericTypeId)) {
            throw new IllegalStateException("A component container factory for " + genericTypeId + " already exists.");
        }
        Map<Identifier, MethodHandleInfo> componentFactories = this.componentFactories.getOrDefault(genericTypeId, Collections.emptyMap());
        for (MethodHandleInfo factory : componentFactories.values()) {
            MethodType factoryType = factory.getMethodType();
            if (!expectedComponentClass.isAssignableFrom(factoryType.returnType())) {
                throw new StaticComponentLoadingException("Bad return type in component factory " + factory + ", expected " + expectedComponentClass + " or a subclass");
            }
            if (factoryType.parameterCount() > argClasses.length) {
                throw new StaticComponentLoadingException(
                    String.format("Too many arguments in method %s. Should be at most %d arguments with types %s.", factory, factoryType.parameterCount(), factoryType.parameterList().stream().map(Class::getTypeName).collect(Collectors.joining(", ", "[", "]")))
                );
            }
            for (int i = 0; i < factoryType.parameterCount(); i++) {
                if (!Objects.equals(factoryType.parameterType(i), argClasses[i])) {
                    throw new StaticComponentLoadingException("Invalid argument " + argClasses[i].getSimpleName() + " in component factory " + factory + ", expected " + argClasses[i].getSimpleName());
                }
            }
        }
        Class<? extends ComponentContainer<?>> containerClass = StaticComponentPluginBase.spinComponentContainer(componentFactories.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new MethodData(e.getValue()))), getSuffix(genericTypeId), Arrays.stream(argClasses).map(Type::getType).toArray(Type[]::new));
        this.claimedFactories.add(genericTypeId);
        return containerClass;
    }

    Class<? extends FeedbackContainerFactory<?, ?>> spinSingleArgContainerFactory(Identifier genericTypeId, Class<? extends Component> componentClass, Class<?> argClass) throws IOException {
        this.ensureInitialized();
        Class<? extends ComponentContainer<?>> containerClass = this.spinComponentContainer(genericTypeId, componentClass, argClass);
        return StaticComponentPluginBase.spinSingleArgFactory(getSuffix(genericTypeId), Type.getType(containerClass), Type.getType(argClass));
    }

    @Override
    public void register(Identifier componentId, Identifier providerId, MethodHandle factory) {
        MethodHandleInfo factoryInfo = this.lookup.revealDirect(factory);
        Map<Identifier, MethodHandleInfo> specializedMap = this.componentFactories.computeIfAbsent(providerId, t -> new HashMap<>());
        MethodHandleInfo previousFactory = specializedMap.get(componentId);
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + componentId + " on provider '" + providerId + "': " + factoryInfo + " and " + previousFactory);
        }
        specializedMap.put(componentId, factoryInfo);
    }

    @Override
    protected void init() {
        CcaBootstrap.INSTANCE.processSpecializedInitializers(StaticGenericComponentInitializer.class,
            initializer -> initializer.registerGenericComponentFactories(this, this.lookup));
    }
}
