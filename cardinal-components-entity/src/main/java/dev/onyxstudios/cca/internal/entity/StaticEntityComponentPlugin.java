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
package dev.onyxstudios.cca.internal.entity;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactory;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.CcaAsmHelper;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public final class StaticEntityComponentPlugin extends LazyDispatcher implements EntityComponentFactoryRegistry {
    public static final StaticEntityComponentPlugin INSTANCE = new StaticEntityComponentPlugin();

    private StaticEntityComponentPlugin() {
        super("instantiating an entity");
    }

    private static String getSuffix(Class<?> entityClass) {
        String simpleName = entityClass.getSimpleName();
        return String.format("EntityImpl_%s_%s", simpleName, Integer.toHexString(entityClass.getName().hashCode()));
    }

    private final List<PredicatedComponentFactory<?>> dynamicFactories = new ArrayList<>();
    private final Map<Class<? extends Entity>, Map<ComponentKey<?>, Class<? extends Component>>> componentImpls = new HashMap<>();
    private final Map<Class<? extends Entity>, Map<ComponentKey<?>, EntityComponentFactory<?, ?>>> componentFactories = new HashMap<>();
    private final Map<Class<? extends Entity>, Class<? extends ComponentContainer>> containerClasses = new HashMap<>();
    private final Map<Key, Class<? extends DynamicContainerFactory<?>>> factoryClasses = new HashMap<>();

    public boolean requiresStaticFactory(Class<? extends Entity> entityClass) {
        this.ensureInitialized();
        return entityClass == Entity.class || this.componentFactories.containsKey(entityClass);
    }

    public Class<? extends DynamicContainerFactory<?>> spinDedicatedFactory(Key key) {
        this.ensureInitialized();

        // we need a cache as this method is called for a given class each time one of its subclasses is loaded.
        return this.factoryClasses.computeIfAbsent(key, k -> {
            for (PredicatedComponentFactory<?> dynamicFactory : this.dynamicFactories) {
                dynamicFactory.tryRegister(k.entityClass);
            }

            Class<? extends Entity> entityClass = k.entityClass;

            Map<ComponentKey<?>, EntityComponentFactory<?, ?>> compiled = new LinkedHashMap<>(this.componentFactories.getOrDefault(entityClass, Collections.emptyMap()));
            Map<ComponentKey<?>, Class<? extends Component>> compiledImpls = new LinkedHashMap<>(this.componentImpls.getOrDefault(entityClass, Collections.emptyMap()));
            Class<?> type = entityClass;

            while (type != Entity.class) {
                type = type.getSuperclass();
                this.componentFactories.getOrDefault(type, Collections.emptyMap()).forEach(compiled::putIfAbsent);
                this.componentImpls.getOrDefault(type, Collections.emptyMap()).forEach(compiledImpls::putIfAbsent);
            }

            String implSuffix = getSuffix(entityClass);

            try {
                Class<? extends ComponentContainer> containerCls = this.containerClasses.get(entityClass);
                if (containerCls == null) {
                    containerCls = CcaAsmHelper.spinComponentContainer(EntityComponentFactory.class, compiled, compiledImpls, implSuffix);
                    this.containerClasses.put(entityClass, containerCls);
                }
                return StaticComponentPluginBase.spinContainerFactory(implSuffix + "_" + k.eventCount, DynamicContainerFactory.class, containerCls, EntityComponentCallback.class, k.eventCount, entityClass);
            } catch (IOException e) {
                throw new StaticComponentLoadingException("Failed to generate a dedicated component container for " + entityClass, e);
            }
        });
    }

    @Override
    protected void init() {
        StaticComponentPluginBase.processInitializers(
            FabricLoader.getInstance().getEntrypointContainers("cardinal-components-entity", EntityComponentInitializer.class),
            initializer -> initializer.registerEntityComponentFactories(this)
        );
    }

    @Override
    public <C extends Component, E extends Entity> void registerFor(Class<E> target, ComponentKey<C> type, EntityComponentFactory<? extends C, E> factory) {
        this.checkLoading(EntityComponentFactoryRegistry.class, "register");
        this.register0(target, type, factory, type.getComponentClass());
    }

    @Override
    public <C extends Component> void registerFor(Predicate<Class<? extends Entity>> test, ComponentKey<C> type, EntityComponentFactory<C, Entity> factory) {
        this.dynamicFactories.add(new PredicatedComponentFactory<>(test, type, factory, type.getComponentClass()));
    }

    @Override
    public <C extends Component, E extends Entity> Registration<C, E> beginRegistration(Class<E> target, ComponentKey<C> key) {
        return new RegistrationImpl<>(target, key);
    }

    @Override
    public <C extends PlayerComponent<? super C>> void registerForPlayers(ComponentKey<? super C> key, EntityComponentFactory<C, PlayerEntity> factory) {
        this.registerForPlayers(key, factory, CardinalEntityInternals.DEFAULT_COPY_STRATEGY);
    }

    @Override
    public <C extends Component, P extends C> void registerForPlayers(ComponentKey<C> key, EntityComponentFactory<P, PlayerEntity> factory, RespawnCopyStrategy<? super P> respawnStrategy) {
        this.registerFor(PlayerEntity.class, key, factory);
        CardinalEntityInternals.registerRespawnCopyStrat(key, respawnStrategy);
    }

    @Override
    public <C extends Component> void setRespawnCopyStrategy(ComponentKey<C> type, RespawnCopyStrategy<? super C> strategy) {
        CardinalEntityInternals.registerRespawnCopyStrat(type, strategy);
    }

    private <C extends Component, F extends C, E extends Entity> void register0(Class<? extends E> target, ComponentKey<? super C> key, EntityComponentFactory<F, E> factory, Class<C> impl) {
        Map<ComponentKey<?>, EntityComponentFactory<?, ?>> specializedMap = this.componentFactories.computeIfAbsent(target, t -> new LinkedHashMap<>());
        EntityComponentFactory<?, ?> previousFactory = specializedMap.get(key);
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + key.getId() + " on " + target + ": " + factory + " and " + previousFactory);
        }
        EntityComponentFactory<Component, E> checked = entity -> Objects.requireNonNull(((EntityComponentFactory<?, E>) factory).createForEntity(entity), "Component factory "+ factory + " for " + key.getId() + " returned null on " + target.getSimpleName());
        this.componentImpls.computeIfAbsent(target, t -> new LinkedHashMap<>()).put(key, impl);
        specializedMap.put(key, checked);
    }

    static class Key {
        final int eventCount;
        final Class<? extends Entity> entityClass;

        public Key(int eventCount, Class<? extends Entity> entityClass) {
            this.eventCount = eventCount;
            this.entityClass = entityClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return this.eventCount == key.eventCount &&
                this.entityClass.equals(key.entityClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.eventCount, this.entityClass);
        }
    }

    private final class PredicatedComponentFactory<C extends Component> {
        private final Predicate<Class<? extends Entity>> predicate;
        private final ComponentKey<? super C> type;
        private final EntityComponentFactory<C, Entity> factory;
        private final Class<C> impl;

        public PredicatedComponentFactory(Predicate<Class<? extends Entity>> predicate, ComponentKey<? super C> type, EntityComponentFactory<C, Entity> factory, Class<C> impl) {
            this.type = type;
            this.factory = factory;
            this.predicate = predicate;
            this.impl = impl;
        }

        public void tryRegister(Class<? extends Entity> clazz) {
            if (this.predicate.test(clazz)) {
                StaticEntityComponentPlugin.this.register0(clazz, this.type, this.factory, this.impl);
            }
        }
    }

    private final class RegistrationImpl<C extends Component, E extends Entity> implements Registration<C, E> {
        private final Class<E> target;
        private final ComponentKey<? super C> key;
        private Class<C> componentClass;
        private Predicate<Class<? extends E>> test;

        RegistrationImpl(Class<E> target, ComponentKey<C> key) {
            this.target = target;
            this.componentClass = key.getComponentClass();
            this.test = null;
            this.key = key;
        }

        @Override
        public Registration<C, E> filter(Predicate<Class<? extends E>> test) {
            this.test = this.test == null ? test : this.test.and(test);
            return this;
        }

        @Override
        public <I extends C> Registration<I, E> impl(Class<I> impl) {
            @SuppressWarnings("unchecked") RegistrationImpl<I, E> ret = (RegistrationImpl<I, E>) this;
            ret.componentClass = impl;
            return ret;
        }

        @Override
        public Registration<C, E> respawnStrategy(RespawnCopyStrategy<? super C> strategy) {
            CardinalEntityInternals.registerRespawnCopyStrat(this.key, strategy);
            return this;
        }

        @Override
        public void end(EntityComponentFactory<C, E> factory) {
            StaticEntityComponentPlugin.this.checkLoading(Registration.class, "end");
            if (this.test == null) {
                StaticEntityComponentPlugin.this.register0(
                    this.target,
                    this.key,
                    factory,
                    this.componentClass
                );
            } else {
                StaticEntityComponentPlugin.this.dynamicFactories.add(new PredicatedComponentFactory<>(
                    c -> this.target.isAssignableFrom(c) && this.test.test(c.asSubclass(this.target)),
                    key,
                    entity -> factory.createForEntity(this.target.cast(entity)),
                    this.componentClass
                ));
            }
        }
    }
}
