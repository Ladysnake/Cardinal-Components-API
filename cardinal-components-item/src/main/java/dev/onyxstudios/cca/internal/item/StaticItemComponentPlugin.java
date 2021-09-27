/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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

import com.google.common.collect.Iterables;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.CcaAsmHelper;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public final class StaticItemComponentPlugin extends LazyDispatcher implements ItemComponentFactoryRegistry {
    public static final StaticItemComponentPlugin INSTANCE = new StaticItemComponentPlugin();

    private StaticItemComponentPlugin() {
        super("creating an ItemStack");
    }

    private final List<PredicatedComponentFactory<?>> dynamicFactories = new ArrayList<>();
    private final Map<@Nullable Identifier, ComponentContainer.Factory.Builder<ItemStack>> componentFactories = new HashMap<>();
    private final ComponentContainer.Factory<ItemStack> emptyFactory = stack -> ComponentContainer.EMPTY;

    private static String getSuffix(Identifier itemId) {
        return "ItemStackImpl_" + CcaAsmHelper.getJavaIdentifierName(itemId);
    }

    /**
     * Creates a container factory for an item id.
     */
    public static ComponentContainer.Factory<ItemStack> createItemStackContainerFactory(Item item) {
        Identifier itemId = Registry.ITEM.getId(item);
        return INSTANCE.getFactoryClass(item, itemId);
    }

    public ComponentContainer.Factory<ItemStack> getFactoryClass(Item item, Identifier itemId) {
        this.ensureInitialized();
        Objects.requireNonNull(item);

        for (PredicatedComponentFactory<?> dynamicFactory : this.dynamicFactories) {
            dynamicFactory.tryRegister(item, itemId);
        }

        if (this.componentFactories.containsKey(itemId)) {
            return this.componentFactories.get(itemId).factoryNameSuffix(getSuffix(itemId)).build();
        }

        return this.emptyFactory;
    }

    @Override
    protected void init() {
        StaticComponentPluginBase.processInitializers(
            StaticComponentPluginBase.getComponentEntrypoints("cardinal-components-item", ItemComponentInitializer.class),
            initializer -> initializer.registerItemComponentFactories(this)
        );
    }

    public <C extends Component> void registerFor(Identifier itemId, ComponentKey<C> type, ComponentFactory<ItemStack, ? extends C> factory) {
        this.checkLoading(ItemComponentFactoryRegistry.class, "register");
        this.register0(itemId, type, factory);
    }

    public <C extends Component> void registerFor(Item item, ComponentKey<C> type, ComponentFactory<ItemStack, ? extends C> factory) {
        if (!Iterables.contains(Registry.ITEM, item)) {
            throw new IllegalStateException(item + " must be registered to Registry.ITEM before using it for component registration");
        }
        Identifier id = Registry.ITEM.getId(item);
        this.registerFor(id, type, factory);
    }

    @Override
    public <C extends ItemComponent> void register(Predicate<Item> test, ComponentKey<? super C> type, ComponentFactory<ItemStack, C> factory) {
        this.registerFor(test, type, ItemComponent.wrapFactory(type, factory));
    }

    @Override
    public <C extends ItemComponent> void register(Item item, ComponentKey<? super C> type, ComponentFactory<ItemStack, C> factory) {
        this.registerFor(item, type, ItemComponent.wrapFactory(type, factory));
    }

    @Override
    public <C extends TransientComponent> void registerTransient(Predicate<Item> test, ComponentKey<? super C> type, ComponentFactory<ItemStack, C> factory) {
        this.registerFor(test, type, factory);
    }

    @Override
    public <C extends TransientComponent> void registerTransient(Item item, ComponentKey<? super C> type, ComponentFactory<ItemStack, C> factory) {
        this.registerFor(item, type, factory);
    }

    private <C extends Component> void registerFor(Predicate<Item> test, ComponentKey<C> type, ComponentFactory<ItemStack, ? extends C> factory) {
        this.dynamicFactories.add(new PredicatedComponentFactory<>(test, type, factory));
    }

    private <C extends Component> void register0(Identifier itemId, ComponentKey<C> type, ComponentFactory<ItemStack, ? extends C> factory) {
        Objects.requireNonNull(itemId);

        ComponentContainer.Factory.Builder<ItemStack> builder = this.componentFactories.computeIfAbsent(itemId, t -> ComponentContainer.Factory.builder(ItemStack.class));
        builder.checkDuplicate(type, previousFactory -> "Duplicate factory declarations for " + type.getId() + " on item '" + itemId + "': " + factory + " and " + previousFactory);
        builder.component(type, factory);
    }

    private final class PredicatedComponentFactory<C extends Component> {
        private final Predicate<Item> predicate;
        private final ComponentKey<C> type;
        private final ComponentFactory<ItemStack, ? extends C> factory;

        public PredicatedComponentFactory(Predicate<Item> predicate, ComponentKey<C> type, ComponentFactory<ItemStack, ? extends C> factory) {
            this.type = type;
            this.factory = factory;
            this.predicate = predicate;
        }

        public void tryRegister(Item item, Identifier id) {
            if (this.predicate.test(item)) {
                StaticItemComponentPlugin.this.register0(id, this.type, this.factory);
            }
        }
    }
}
