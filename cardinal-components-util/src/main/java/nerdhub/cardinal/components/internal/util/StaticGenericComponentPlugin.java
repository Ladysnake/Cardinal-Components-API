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
import nerdhub.cardinal.components.internal.FeedbackContainerFactory;
import nerdhub.cardinal.components.internal.StatefulLazy;
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
import java.util.*;
import java.util.stream.Collectors;

public final class StaticGenericComponentPlugin extends StatefulLazy implements GenericComponentFactoryRegistry {
    public static final StaticGenericComponentPlugin INSTANCE = new StaticGenericComponentPlugin();
    private final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    private static String getSuffix(Identifier itemId) {
        return "GenericImpl_" + CcaAsmHelper.getJavaIdentifierName(itemId);
    }

    private final Map<Identifier, Map</*ComponentType*/Identifier, MethodData>> componentFactories = new HashMap<>();
    private final Set<Identifier> claimedFactories = new LinkedHashSet<>();

    Class<? extends ComponentContainer<?>> spinComponentContainer(Identifier genericTypeId, Class<? extends Component> expectedComponentClass, Class<?>... argClasses) throws IOException {
        this.ensureInitialized();

        Type rType = Type.getType(expectedComponentClass);
        Type[] args = new Type[argClasses.length];
        for (int i = 0; i < argClasses.length; i++) {
            args[i] = Type.getType(argClasses[i]);
        }
        if (this.claimedFactories.contains(genericTypeId)) {
            throw new IllegalStateException("A component container factory for " + genericTypeId + " already exists.");
        }
        Map<Identifier, MethodData> componentFactories = this.componentFactories.getOrDefault(genericTypeId, Collections.emptyMap());
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

    Class<? extends FeedbackContainerFactory<?, ?>> spinSingleArgContainerFactory(Identifier genericTypeId, Class<? extends Component> componentClass, Class<?> argClass) throws IOException {
        this.ensureInitialized();
        Class<? extends ComponentContainer<?>> containerClass = this.spinComponentContainer(genericTypeId, componentClass, argClass);
        return StaticComponentPluginBase.spinSingleArgFactory(getSuffix(genericTypeId), Type.getType(containerClass), Type.getType(argClass));
    }

    @Override
    public void register(Identifier componentId, Identifier providerId, MethodHandle factory) {
        MethodHandleInfo factoryInfo = this.lookup.revealDirect(factory);
        Map<Identifier, MethodData> specializedMap = this.componentFactories.computeIfAbsent(providerId, t -> new HashMap<>());
        MethodData previousFactory = specializedMap.get(componentId);
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + componentId + " on provider '" + providerId + "': " + factory + " and " + previousFactory);
        }
        specializedMap.put(componentId, new MethodData(factoryInfo));
    }

    @Override
    protected void init() {
        CcaBootstrap.INSTANCE.processSpecializedInitializers(StaticGenericComponentInitializer.class,
            initializer -> initializer.registerGenericComponentFactories(this, this.lookup));
    }
}
