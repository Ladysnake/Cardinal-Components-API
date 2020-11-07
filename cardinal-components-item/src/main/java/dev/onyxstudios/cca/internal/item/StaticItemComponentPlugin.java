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

import dev.onyxstudios.cca.api.v3.component.*;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

public final class StaticItemComponentPlugin extends LazyDispatcher implements ItemComponentFactoryRegistry {
    public static final StaticItemComponentPlugin INSTANCE = new StaticItemComponentPlugin();
    private static final boolean DEV = Boolean.getBoolean("fabric.development");
    private static final boolean VERIFY_EQUALS = DEV && !Boolean.getBoolean("cca.debug.noverifyequals");

    private StaticItemComponentPlugin() {
        super("creating an ItemStack");
    }

    private final List<PredicatedComponentFactory<?>> dynamicFactories = new ArrayList<>();
    private final Map<@Nullable Identifier, ComponentContainer.Factory.Builder<ItemStack>> componentFactories = new HashMap<>();
    private final ComponentContainer.Factory<ItemStack> emptyFactory = stack -> ComponentContainer.EMPTY;

    public ComponentContainer.Factory<ItemStack> getFactoryClass(Item item, Identifier itemId) {
        this.ensureInitialized();
        Objects.requireNonNull(item);

        for (PredicatedComponentFactory<?> dynamicFactory : this.dynamicFactories) {
            dynamicFactory.tryRegister(item, itemId);
        }

        if (this.componentFactories.containsKey(itemId)) {
            return this.componentFactories.get(itemId).build();
        }

        return this.emptyFactory;
    }

    @Override
    protected void init() {
        StaticComponentPluginBase.processInitializers(
            FabricLoader.getInstance().getEntrypointContainers("cardinal-components-item", ItemComponentInitializer.class),
            initializer -> initializer.registerItemComponentFactories(this)
        );
    }

    @Override
    public <C extends Component> void registerFor(Identifier itemId, ComponentKey<C> type, ComponentFactory<ItemStack, ? extends C> factory) {
        this.checkLoading(ItemComponentFactoryRegistry.class, "register");
        this.register0(itemId, type, factory);
    }

    @Override
    public <C extends Component> void registerFor(Predicate<Item> test, ComponentKey<C> type, ComponentFactory<ItemStack, ? extends C> factory) {
        this.dynamicFactories.add(new PredicatedComponentFactory<>(test, type, factory));
    }

    private <C extends Component> void register0(Identifier itemId, ComponentKey<C> type, ComponentFactory<ItemStack, ? extends C> factory) {
        Objects.requireNonNull(itemId);

        ComponentContainer.Factory.Builder<ItemStack> builder = this.componentFactories.computeIfAbsent(itemId, t -> ComponentContainer.Factory.builder(ItemStack.class));
        builder.checkDuplicate(type, previousFactory -> "Duplicate factory declarations for " + type.getId() + " on item '" + itemId + "': " + factory + " and " + previousFactory);

        ComponentFactory<ItemStack, ? extends C> finalFactory;

        if (VERIFY_EQUALS && ComponentV3.class.isAssignableFrom(type.getComponentClass())) {
            finalFactory = new ComponentFactory<ItemStack, C>() {
                private boolean checked;

                @Nonnull
                @Override
                public C createComponent(ItemStack stack) {
                    C component = factory.createComponent(stack);

                    if (!this.checked) {
                        try {
                            if (component.getClass().getMethod("equals", Object.class).getDeclaringClass() == Object.class) {
                                throw new IllegalStateException("Component implementation " + component.getClass().getTypeName() + " attached to " + stack + " should override Object#equals.\nMore information: https://github.com/OnyxStudios/Cardinal-Components-API/wiki/Cardinal-Components-Item");
                            }
                        } catch (NoSuchMethodException e) {
                            throw new AssertionError("Object#equals not found ?!");
                        }

                        this.checked = true;
                    }

                    return component;
                }
            };
        } else {
            finalFactory = factory;
        }

        builder.component(type, finalFactory);
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
