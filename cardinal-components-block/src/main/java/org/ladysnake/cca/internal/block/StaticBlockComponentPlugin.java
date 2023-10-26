/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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
package org.ladysnake.cca.internal.block;

import org.ladysnake.cca.api.v3.block.BlockComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.block.BlockComponentInitializer;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import org.ladysnake.cca.internal.base.LazyDispatcher;
import org.ladysnake.cca.internal.base.QualifiedComponentFactory;
import org.ladysnake.cca.internal.base.asm.StaticComponentLoadingException;
import org.ladysnake.cca.internal.base.asm.StaticComponentPluginBase;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class StaticBlockComponentPlugin extends LazyDispatcher implements BlockComponentFactoryRegistry {
    public static final StaticBlockComponentPlugin INSTANCE = new StaticBlockComponentPlugin();

    private static String getSuffix(Class<? extends BlockEntity> key) {
        return "BlockEntityImpl_%s_%s".formatted(key.getSimpleName(), Integer.toHexString(key.getName().hashCode()));
    }

    private StaticBlockComponentPlugin() {
        super("creating a BlockEntity");
    }

    private final List<PredicatedComponentFactory<?>> dynamicFactories = new ArrayList<>();
    private final Map<Class<? extends BlockEntity>, Map<ComponentKey<?>, QualifiedComponentFactory<ComponentFactory<? extends BlockEntity, ?>>>> beComponentFactories = new Reference2ObjectOpenHashMap<>();
    private final Set<Class<? extends BlockEntity>> clientTicking = new ReferenceOpenHashSet<>();
    private final Set<Class<? extends BlockEntity>> serverTicking = new ReferenceOpenHashSet<>();

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getComponentTicker(World world, T be, @Nullable BlockEntityTicker<T> base) {
        if (world.isClient && this.clientTicking.contains(be.getClass())) {
            if (base == null) return (w, pos, state, blockEntity) -> blockEntity.asComponentProvider().getComponentContainer().tickClientComponents();
            return (w, pos, state, blockEntity) -> {
                blockEntity.asComponentProvider().getComponentContainer().tickClientComponents();
                base.tick(w, pos, state, blockEntity);
            };
        } else if (!world.isClient && this.serverTicking.contains(be.getClass())) {
            if (base == null) return (w, pos, state, blockEntity) -> blockEntity.asComponentProvider().getComponentContainer().tickServerComponents();
            return (w, pos, state, blockEntity) -> {
                blockEntity.asComponentProvider().getComponentContainer().tickServerComponents();
                base.tick(w, pos, state, blockEntity);
            };
        }
        return base;
    }

    public boolean requiresStaticFactory(Class<? extends BlockEntity> entityClass) {
        StaticBlockComponentPlugin.INSTANCE.ensureInitialized();

        for (PredicatedComponentFactory<?> dynamicFactory : this.dynamicFactories) {
            dynamicFactory.tryRegister(entityClass);
        }

        return entityClass == BlockEntity.class || this.beComponentFactories.containsKey(entityClass);
    }

    public ComponentContainer.Factory<BlockEntity> buildDedicatedFactory(Class<? extends BlockEntity> entityClass) {
        StaticBlockComponentPlugin.INSTANCE.ensureInitialized();

        var compiled = new LinkedHashMap<>(this.beComponentFactories.getOrDefault(entityClass, Collections.emptyMap()));
        Class<? extends BlockEntity> type = entityClass;

        while (type != BlockEntity.class) {
            type = type.getSuperclass().asSubclass(BlockEntity.class);
            for (var e : this.beComponentFactories.getOrDefault(type, Collections.emptyMap()).entrySet()) {
                compiled.putIfAbsent(e.getKey(), e.getValue());
            }
        }

        ComponentContainer.Factory.Builder<BlockEntity> builder = ComponentContainer.Factory.builder(BlockEntity.class)
            .factoryNameSuffix(getSuffix(entityClass));

        for (var entry : compiled.entrySet()) {
            addToBuilder(builder, entry);
            if (ClientTickingComponent.class.isAssignableFrom(entry.getValue().impl())) this.clientTicking.add(entityClass);
            if (ServerTickingComponent.class.isAssignableFrom(entry.getValue().impl())) this.serverTicking.add(entityClass);
        }

        return builder.build();
    }

    private <C extends Component> void addToBuilder(ComponentContainer.Factory.Builder<BlockEntity> builder, Map.Entry<ComponentKey<?>, QualifiedComponentFactory<ComponentFactory<? extends BlockEntity, ?>>> entry) {
        @SuppressWarnings("unchecked") var key = (ComponentKey<C>) entry.getKey();
        @SuppressWarnings("unchecked") var factory = (ComponentFactory<BlockEntity, C>) entry.getValue().factory();
        @SuppressWarnings("unchecked") var impl = (Class<C>) entry.getValue().impl();
        builder.component(key, impl, factory, entry.getValue().dependencies());
    }

    public <C extends Component, E extends BlockEntity> void registerFor(Class<E> target, ComponentKey<C> type, ComponentFactory<E, C> factory) {
        this.checkLoading(BlockComponentFactoryRegistry.class, "register");
        this.register0(target, type, new QualifiedComponentFactory<>(factory, type.getComponentClass(), Set.of()));
    }

    private <C extends Component, F extends C, E extends BlockEntity> void register0(Class<? extends E> target, ComponentKey<? super C> type, QualifiedComponentFactory<ComponentFactory<E, F>> factory) {
        var specializedMap = this.beComponentFactories.computeIfAbsent(target, t -> new LinkedHashMap<>());
        var previousFactory = specializedMap.get(type);

        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for %s on %s: %s and %s".formatted(type.getId(), target, factory, previousFactory));
        }

        @SuppressWarnings("unchecked") var factory1 = (QualifiedComponentFactory<ComponentFactory<? extends BlockEntity, ?>>) (QualifiedComponentFactory<?>) factory;
        specializedMap.put(type, factory1);
    }

    @Override
    public <C extends Component, B extends BlockEntity> Registration<C, B> beginRegistration(Class<B> target, ComponentKey<C> key) {
        return new RegistrationImpl<>(target, key);
    }

    @Override
    protected void init() {
        StaticComponentPluginBase.processInitializers(
            StaticComponentPluginBase.getComponentEntrypoints("cardinal-components-block", BlockComponentInitializer.class),
            initializer -> initializer.registerBlockComponentFactories(this)
        );
    }

    private final class PredicatedComponentFactory<C extends Component> {
        private final Predicate<Class<? extends BlockEntity>> predicate;
        private final ComponentKey<? super C> type;
        private final QualifiedComponentFactory<ComponentFactory<BlockEntity, C>> factory;

        public PredicatedComponentFactory(Predicate<Class<? extends BlockEntity>> predicate, ComponentKey<? super C> type, QualifiedComponentFactory<ComponentFactory<BlockEntity, C>> factory) {
            this.type = type;
            this.factory = factory;
            this.predicate = predicate;
        }

        public void tryRegister(Class<? extends BlockEntity> clazz) {
            if (this.predicate.test(clazz)) {
                StaticBlockComponentPlugin.this.register0(clazz, this.type, this.factory);
            }
        }
    }

    private final class RegistrationImpl<C extends Component, E extends BlockEntity> implements Registration<C, E> {
        private final Class<E> target;
        private final ComponentKey<? super C> key;
        private final Set<ComponentKey<?>> dependencies;
        private Class<C> componentClass;
        private Predicate<Class<? extends E>> test;

        RegistrationImpl(Class<E> target, ComponentKey<C> key) {
            this.target = target;
            this.componentClass = key.getComponentClass();
            this.dependencies = new LinkedHashSet<>();
            this.test = null;
            this.key = key;
        }

        @Override
        public Registration<C, E> filter(Predicate<Class<? extends E>> test) {
            this.test = this.test == null ? test : this.test.and(test);
            return this;
        }

        @Override
        public Registration<C, E> after(ComponentKey<?> dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        @Override
        public <I extends C> Registration<I, E> impl(Class<I> impl) {
            @SuppressWarnings("unchecked") RegistrationImpl<I, E> ret = (RegistrationImpl<I, E>) this;
            ret.componentClass = impl;
            return ret;
        }

        @Override
        public void end(ComponentFactory<E, C> factory) {
            StaticBlockComponentPlugin.this.checkLoading(Registration.class, "end");
            if (this.test == null) {
                StaticBlockComponentPlugin.this.register0(
                    this.target,
                    this.key,
                    new QualifiedComponentFactory<>(factory, this.componentClass, this.dependencies)
                );
            } else {
                StaticBlockComponentPlugin.this.dynamicFactories.add(new PredicatedComponentFactory<>(
                    c -> this.target.isAssignableFrom(c) && this.test.test(c.asSubclass(this.target)),
                    this.key,
                    new QualifiedComponentFactory<>(
                        entity -> factory.createComponent(this.target.cast(entity)),
                        this.componentClass,
                        this.dependencies
                    )
                ));
            }
        }
    }
}
