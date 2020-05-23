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
import nerdhub.cardinal.components.api.component.ItemComponentFactory;
import nerdhub.cardinal.components.api.component.ItemComponentFactoryRegistry;
import nerdhub.cardinal.components.api.component.StaticItemComponentInitializer;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.CcaBootstrap;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import nerdhub.cardinal.components.internal.asm.StaticComponentPluginBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class StaticItemComponentPlugin extends LazyDispatcher implements ItemComponentFactoryRegistry {
    public static final StaticItemComponentPlugin INSTANCE = new StaticItemComponentPlugin();
    public static final String WILDARD_IMPL_SUFFIX = "ItemStackImpl_All";

    public StaticItemComponentPlugin() {
        super("creating an ItemStack");
    }

    private static String getSuffix(Identifier itemId) {
        return "ItemStackImpl_" + CcaAsmHelper.getJavaIdentifierName(itemId);
    }

    private final Map<@Nullable Identifier, Map</*ComponentType*/Identifier, ItemComponentFactory<?>>> componentFactories = new HashMap<>();
    private final Map<Identifier, Class<? extends DynamicContainerFactory<?,?>>> factoryClasses = new HashMap<>();
    private Class<? extends DynamicContainerFactory<?, ?>> wildcardFactoryClass;

    public Class<? extends DynamicContainerFactory<?,?>> getFactoryClass(Identifier itemId) {
        this.ensureInitialized();
        Class<? extends DynamicContainerFactory<?,?>> specificFactory = this.factoryClasses.get(itemId);
        if (specificFactory != null) {
            return specificFactory;
        }
        assert this.wildcardFactoryClass != null;
        return this.wildcardFactoryClass;
    }

    @Override
    public void register(Identifier componentId, @Nullable Identifier itemId, ItemComponentFactory<?> factory) {
        this.checkLoading(ItemComponentFactoryRegistry.class, "register");
        Map<Identifier, ItemComponentFactory<?>> specializedMap = this.componentFactories.computeIfAbsent(itemId, t -> new HashMap<>());
        ItemComponentFactory<?> previousFactory = specializedMap.get(componentId);
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + componentId + " on " + (itemId == null ? "every item" : "item '" + itemId + "'") + ": " + factory + " and " + previousFactory);
        }
        specializedMap.put(componentId, factory);
    }

    @Override
    protected void init() {
        CcaBootstrap.INSTANCE.processSpecializedInitializers(StaticItemComponentInitializer.class,
            (initializer, provider) -> initializer.registerItemComponentFactories(this));
        Map<Identifier, ItemComponentFactory<?>> wildcardMap = this.componentFactories.getOrDefault(null, Collections.emptyMap());
        try {
            Class<? extends ComponentContainer<?>> containerCls = StaticComponentPluginBase.spinComponentContainer(ItemComponentFactory.class, wildcardMap, WILDARD_IMPL_SUFFIX);
            this.wildcardFactoryClass = StaticComponentPluginBase.spinContainerFactory(WILDARD_IMPL_SUFFIX, DynamicContainerFactory.class, containerCls, ItemComponentCallback.class, 2, ItemStack.class);
        } catch (IOException e) {
            throw new StaticComponentLoadingException("Failed to generate the fallback component container for item stacks", e);
        }
        for (Map.Entry<Identifier, Map<Identifier, ItemComponentFactory<?>>> entry : this.componentFactories.entrySet()) {
            if (entry.getKey() == null) continue;
            try {
                Map<Identifier, ItemComponentFactory<?>> compiled = new HashMap<>(entry.getValue());
                wildcardMap.forEach(compiled::putIfAbsent);
                String implSuffix = getSuffix(entry.getKey());
                Class<? extends ComponentContainer<?>> containerCls = StaticComponentPluginBase.spinComponentContainer(ItemComponentFactory.class, compiled, implSuffix);
                this.factoryClasses.put(entry.getKey(), StaticComponentPluginBase.spinContainerFactory(implSuffix, DynamicContainerFactory.class, containerCls, ItemComponentCallback.class, 2, ItemStack.class));
            } catch (IOException e) {
                throw new StaticComponentLoadingException("Failed to generate a dedicated component container for " + entry.getKey(), e);
            }
        }
    }
}
