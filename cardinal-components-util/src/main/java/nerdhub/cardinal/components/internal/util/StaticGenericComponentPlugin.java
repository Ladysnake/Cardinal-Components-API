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

import com.google.common.reflect.TypeToken;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.GenericComponentFactoryRegistry;
import nerdhub.cardinal.components.api.component.StaticGenericComponentInitializer;
import nerdhub.cardinal.components.internal.CcaBootstrap;
import nerdhub.cardinal.components.internal.DispatchingLazy;
import nerdhub.cardinal.components.internal.StaticComponentPluginBase;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public final class StaticGenericComponentPlugin extends DispatchingLazy implements GenericComponentFactoryRegistry {
    public static final StaticGenericComponentPlugin INSTANCE = new StaticGenericComponentPlugin();
    private ModContainer currentProvider;

    public StaticGenericComponentPlugin() {
        super("initializing something");
    }

    private static String getSuffix(Identifier itemId) {
        return "GenericImpl_" + CcaAsmHelper.getJavaIdentifierName(itemId);
    }

    private final Map<Identifier, Map</*ComponentType*/Identifier, OwnedObject<?>>> componentFactories = new HashMap<>();
    private final Set<Identifier> claimedFactories = new LinkedHashSet<>();

    <I> Class<? extends ComponentContainer<?>> spinComponentContainer(TypeToken<I> componentFactoryType, Identifier genericTypeId) throws IOException {
        this.ensureInitialized();

        if (this.claimedFactories.contains(genericTypeId)) {
            throw new IllegalStateException("A component container factory for " + genericTypeId + " already exists.");
        }
        Map<Identifier, OwnedObject<?>> componentFactories = this.componentFactories.getOrDefault(genericTypeId, Collections.emptyMap());
        Map<Identifier, I> resolved = new LinkedHashMap<>();
        for (Map.Entry<Identifier, OwnedObject<?>> entry : componentFactories.entrySet()) {
            Object object = entry.getValue().object;
            if (!componentFactoryType.isSupertypeOf(entry.getValue().type)) {
                ModMetadata blamed = entry.getValue().owner.getMetadata();
                throw new StaticComponentLoadingException(String.format("Cannot cast %s registered as a component factory for %s by '%s'(%s) to %s", entry.getValue().type, entry.getKey(), blamed.getName(), blamed.getId(), componentFactoryType));
            }
            @SuppressWarnings("unchecked") I i = (I) object;
            resolved.put(entry.getKey(), i);
        }
        Class<? extends ComponentContainer<?>> containerClass = StaticComponentPluginBase.spinComponentContainer(componentFactoryType.getRawType(), resolved, getSuffix(genericTypeId));
        this.claimedFactories.add(genericTypeId);
        return containerClass;
    }

    <R> Class<? extends R> spinSingleArgContainerFactory(TypeToken<?> componentFactoryType, Identifier genericProviderId, Class<? super R> containerFactoryType, @Nullable Class<?> componentCallbackType, int eventCount, Class<?>[] actualFactoryArgs) throws IOException {
        this.ensureInitialized();
        Class<? extends ComponentContainer<?>> containerClass = this.spinComponentContainer(componentFactoryType, genericProviderId);
        return StaticComponentPluginBase.spinContainerFactory(getSuffix(genericProviderId), containerFactoryType, containerClass, componentCallbackType, eventCount, actualFactoryArgs);
    }

    @Override
    public <F> void register(Identifier componentId, Identifier providerId, TypeToken<F> componentFactoryType, F factory) {
        this.checkLoading(GenericComponentFactoryRegistry.class, "register");
        Map<Identifier, OwnedObject<?>> specializedMap = this.componentFactories.computeIfAbsent(providerId, t -> new HashMap<>());
        Object previousFactory = specializedMap.get(componentId);
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + componentId + " on provider '" + providerId + "': " + factory + " and " + previousFactory);
        }
        specializedMap.put(componentId, new OwnedObject<>(this.currentProvider, componentFactoryType, factory));
    }

    @Override
    protected void init() {
        CcaBootstrap.INSTANCE.processSpecializedInitializers(StaticGenericComponentInitializer.class,
            (initializer, provider) -> {
                try {
                    this.currentProvider = provider;
                    initializer.registerGenericComponentFactories(this);
                } finally {
                    this.currentProvider = null;
                }
            });
    }

    private static class OwnedObject<T> {
        final ModContainer owner;
        final T object;
        final TypeToken<T> type;

        public OwnedObject(ModContainer owner, TypeToken<T> type, T object) {
            this.owner = owner;
            this.type = type;
            this.object = object;
        }
    }
}
