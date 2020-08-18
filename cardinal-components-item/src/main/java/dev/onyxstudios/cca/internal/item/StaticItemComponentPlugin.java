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

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactory;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryV2;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.CcaAsmHelper;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.event.ItemComponentCallbackV2;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public final class StaticItemComponentPlugin extends LazyDispatcher implements ItemComponentFactoryRegistry {
    public static final StaticItemComponentPlugin INSTANCE = new StaticItemComponentPlugin();
    public static final String WILDCARD_IMPL_SUFFIX = "ItemStackImpl_All";
    private static final boolean DEV = Boolean.getBoolean("fabric.development");
    // TODO enable by default when Component#isComponentEqual is gone
    private static final boolean ENABLE_CHECKS = Boolean.getBoolean("cca.debug.verifyequals");

    private StaticItemComponentPlugin() {
        super("creating an ItemStack");
    }

    private static String getSuffix(Identifier itemId) {
        return "ItemStackImpl_" + CcaAsmHelper.getJavaIdentifierName(itemId);
    }

    private final List<PredicatedComponentFactory<?>> dynamicFactories = new ArrayList<>();
    private final Map<@Nullable Identifier, Map</*ComponentType*/Identifier, ItemComponentFactoryV2<?>>> componentFactories = new HashMap<>();
    private Class<? extends ItemComponentContainerFactory> wildcardFactoryClass;

    public Class<? extends ItemComponentContainerFactory> getFactoryClass(Item item, Identifier itemId) {
        this.ensureInitialized();
        Objects.requireNonNull(item);

        for (PredicatedComponentFactory<?> dynamicFactory : this.dynamicFactories) {
            dynamicFactory.tryRegister(item, itemId);
        }

        if (this.componentFactories.containsKey(itemId)) {
            try {
                Map<Identifier, ItemComponentFactoryV2<?>> compiled = new HashMap<>(this.componentFactories.get(itemId));
                this.getWildcard().forEach(compiled::putIfAbsent);
                String implSuffix = getSuffix(itemId);
                Class<? extends ComponentContainer> containerCls = StaticComponentPluginBase.spinComponentContainer(ItemComponentFactoryV2.class, compiled, implSuffix);
                return StaticComponentPluginBase.spinContainerFactory(implSuffix, ItemComponentContainerFactory.class, containerCls, ItemComponentCallbackV2.class, 2, Item.class, ItemStack.class);
            } catch (IOException e) {
                throw new StaticComponentLoadingException("Failed to generate a dedicated component container for " + itemId, e);
            }
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

        try {
            Class<? extends ComponentContainer> containerCls = StaticComponentPluginBase.spinComponentContainer(ItemComponentFactoryV2.class, this.getWildcard(), WILDCARD_IMPL_SUFFIX);
            this.wildcardFactoryClass = StaticComponentPluginBase.spinContainerFactory(WILDCARD_IMPL_SUFFIX, ItemComponentContainerFactory.class, containerCls, ItemComponentCallbackV2.class, 2, Item.class, ItemStack.class);
        } catch (IOException e) {
            throw new StaticComponentLoadingException("Failed to generate the fallback component container for item stacks", e);
        }
    }

    private Map<Identifier, ItemComponentFactoryV2<?>> getWildcard() {
        return this.componentFactories.getOrDefault(null, Collections.emptyMap());
    }

    @Override
    public <C extends Component> void registerForAll(ComponentKey<C> type, ItemComponentFactory<? extends C> factory) {
        this.register(null, type, factory);
    }

    @Override
    public <C extends Component> void registerForAll(ComponentKey<C> type, ItemComponentFactoryV2<? extends C> factory) {
        this.register(null, type, factory);
    }

    @Override
    public <C extends Component> void registerFor(Identifier itemId, ComponentKey<C> type, ItemComponentFactory<? extends C> factory) {
        Objects.requireNonNull(itemId);
        this.register(itemId, type, factory);
    }

    @Override
    public <C extends Component> void registerFor(Identifier itemId, ComponentKey<C> type, ItemComponentFactoryV2<? extends C> factory) {
        Objects.requireNonNull(itemId);
        this.register(itemId, type, factory);
    }

    @Override
    public <C extends Component> void registerFor(Predicate<Item> test, ComponentKey<C> type, ItemComponentFactoryV2<? extends C> factory) {
        this.dynamicFactories.add(new PredicatedComponentFactory<>(test, type, factory));
    }

    private <C extends Component> void register(@Nullable Identifier itemId, ComponentKey<C> type, ItemComponentFactoryV2<? extends C> factory) {
        this.checkLoading(ItemComponentFactoryRegistry.class, "register");
        this.register0(itemId, type, factory);
    }

    private <C extends Component> void register0(@Nullable Identifier itemId, ComponentKey<C> type, ItemComponentFactoryV2<? extends C> factory) {
        Map<Identifier, ItemComponentFactoryV2<?>> specializedMap = this.componentFactories.computeIfAbsent(itemId, t -> new HashMap<>());
        ItemComponentFactoryV2<?> previousFactory = specializedMap.get(type.getId());
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + type.getId() + " on " + (itemId == null ? "every item" : "item '" + itemId + "'") + ": " + factory + " and " + previousFactory);
        }
        ItemComponentFactoryV2<Component> nonnullFactory = (item, stack) -> Objects.requireNonNull(
            ((ItemComponentFactoryV2<?>) factory).createForStack(item, stack),
            "Component factory " + factory + " for " + type.getId() + " returned null on " + stack
        );
        ItemComponentFactoryV2<Component> finalFactory;

        if (DEV && ENABLE_CHECKS) {
            finalFactory = new ItemComponentFactoryV2<Component>() {
                private boolean checked;

                @Nonnull
                @Override
                public Component createForStack(Item item, ItemStack stack) {
                    Component component = nonnullFactory.createForStack(item, stack);

                    if (!this.checked) {
                        try {
                            if (component.getClass().getMethod("equals", Object.class).getDeclaringClass() == Object.class) {
                                throw new IllegalStateException("Component implementation " + component.getClass().getTypeName() + " attached to " + stack + " does not override Object#equals");
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
            finalFactory = nonnullFactory;
        }

        specializedMap.put(type.getId(), finalFactory);
    }

    private final class PredicatedComponentFactory<C extends Component> {
        private final Predicate<Item> predicate;
        private final ComponentKey<C> type;
        private final ItemComponentFactoryV2<? extends C> factory;

        public PredicatedComponentFactory(Predicate<Item> predicate, ComponentKey<C> type, ItemComponentFactoryV2<? extends C> factory) {
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
