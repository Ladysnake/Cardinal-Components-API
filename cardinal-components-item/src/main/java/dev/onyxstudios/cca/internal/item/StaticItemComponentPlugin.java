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
package dev.onyxstudios.cca.internal.item;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.item.ItemComponentFactory;
import dev.onyxstudios.cca.api.v3.component.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.component.item.ItemComponentFactoryV2;
import dev.onyxstudios.cca.api.v3.component.item.ItemComponentInitializer;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.CcaAsmHelper;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.event.ItemComponentCallbackV2;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class StaticItemComponentPlugin extends LazyDispatcher implements ItemComponentFactoryRegistry {
    public static final StaticItemComponentPlugin INSTANCE = new StaticItemComponentPlugin();
    public static final String WILDARD_IMPL_SUFFIX = "ItemStackImpl_All";

    private StaticItemComponentPlugin() {
        super("creating an ItemStack");
    }

    private static String getSuffix(Identifier itemId) {
        return "ItemStackImpl_" + CcaAsmHelper.getJavaIdentifierName(itemId);
    }

    private final Map<@Nullable Identifier, Map</*ComponentType*/Identifier, ItemComponentFactoryV2<?>>> componentFactories = new HashMap<>();
    private final Map<Identifier, Class<? extends ItemComponentContainerFactory>> factoryClasses = new HashMap<>();
    private Class<? extends ItemComponentContainerFactory> wildcardFactoryClass;

    public Class<? extends ItemComponentContainerFactory> getFactoryClass(Identifier itemId) {
        this.ensureInitialized();
        Class<? extends ItemComponentContainerFactory> specificFactory = this.factoryClasses.get(itemId);
        if (specificFactory != null) {
            return specificFactory;
        }
        assert this.wildcardFactoryClass != null;
        return this.wildcardFactoryClass;
    }

    @Override
    protected void init() {
        StaticComponentPluginBase.processInitializers(
            FabricLoader.getInstance().getEntrypointContainers("cardinal-components-item", ItemComponentInitializer.class),
            initializer -> initializer.registerItemComponentFactories(this)
        );
        Map<Identifier, ItemComponentFactoryV2<?>> wildcardMap = this.componentFactories.getOrDefault(null, Collections.emptyMap());
        try {
            Class<? extends ComponentContainer<?>> containerCls = StaticComponentPluginBase.spinComponentContainer(ItemComponentFactoryV2.class, wildcardMap, WILDARD_IMPL_SUFFIX);
            this.wildcardFactoryClass = StaticComponentPluginBase.spinContainerFactory(WILDARD_IMPL_SUFFIX, ItemComponentContainerFactory.class, containerCls, ItemComponentCallbackV2.class, 2, Item.class, ItemStack.class);
        } catch (IOException e) {
            throw new StaticComponentLoadingException("Failed to generate the fallback component container for item stacks", e);
        }
        for (Map.Entry<Identifier, Map<Identifier, ItemComponentFactoryV2<?>>> entry : this.componentFactories.entrySet()) {
            if (entry.getKey() == null) continue;
            try {
                Map<Identifier, ItemComponentFactoryV2<?>> compiled = new HashMap<>(entry.getValue());
                wildcardMap.forEach(compiled::putIfAbsent);
                String implSuffix = getSuffix(entry.getKey());
                Class<? extends ComponentContainer<?>> containerCls = StaticComponentPluginBase.spinComponentContainer(ItemComponentFactoryV2.class, compiled, implSuffix);
                this.factoryClasses.put(entry.getKey(), StaticComponentPluginBase.spinContainerFactory(implSuffix, ItemComponentContainerFactory.class, containerCls, ItemComponentCallbackV2.class, 2, Item.class, ItemStack.class));
            } catch (IOException e) {
                throw new StaticComponentLoadingException("Failed to generate a dedicated component container for " + entry.getKey(), e);
            }
        }
    }

    @Override
    public <C extends CopyableComponent<?>> void registerForAll(ComponentKey<? super C> type, ItemComponentFactory<C> factory) {
        this.register(null, type, factory);
    }

    @Override
    public <C extends CopyableComponent<?>> void registerForAll(ComponentKey<? super C> type, ItemComponentFactoryV2<C> factory) {
        this.register(null, type, factory);
    }

    @Override
    public <C extends CopyableComponent<?>> void registerFor(Identifier itemId, ComponentKey<? super C> type, ItemComponentFactory<C> factory) {
        Objects.requireNonNull(itemId);
        this.register(itemId, type, factory);
    }

    @Override
    public <C extends CopyableComponent<?>> void registerFor(Identifier itemId, ComponentKey<? super C> type, ItemComponentFactoryV2<C> factory) {
        Objects.requireNonNull(itemId);
        this.register(itemId, type, factory);
    }

    private <C extends CopyableComponent<?>> void register(@Nullable Identifier itemId, ComponentKey<? super C> type, ItemComponentFactoryV2<C> factory) {
        this.checkLoading(ItemComponentFactoryRegistry.class, "register");
        Map<Identifier, ItemComponentFactoryV2<?>> specializedMap = this.componentFactories.computeIfAbsent(itemId, t -> new HashMap<>());
        ItemComponentFactoryV2<?> previousFactory = specializedMap.get(type.getId());
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + type.getId() + " on " + (itemId == null ? "every item" : "item '" + itemId + "'") + ": " + factory + " and " + previousFactory);
        }
        specializedMap.put(type.getId(), (item, stack) -> Objects.requireNonNull(((ItemComponentFactoryV2<?>) factory).createForStack(item, stack), "Component factory "+ factory + " for " + type.getId() + " returned null on " + stack));
    }
}
