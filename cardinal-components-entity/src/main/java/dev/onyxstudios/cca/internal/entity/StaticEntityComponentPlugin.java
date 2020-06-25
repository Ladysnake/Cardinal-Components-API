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

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.entity.EntityComponentFactory;
import dev.onyxstudios.cca.api.v3.component.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.component.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.*;

public final class StaticEntityComponentPlugin extends LazyDispatcher implements EntityComponentFactoryRegistry {
    public static final StaticEntityComponentPlugin INSTANCE = new StaticEntityComponentPlugin();

    private StaticEntityComponentPlugin() {
        super("instantiating an entity");
    }

    private static String getSuffix(Class<?> entityClass) {
        String simpleName = entityClass.getSimpleName();
        return String.format("EntityImpl_%s_%s", simpleName, Integer.toHexString(entityClass.getName().hashCode()));
    }

    private final Map<Class<? extends Entity>, Map</*ComponentType*/Identifier, EntityComponentFactory<?, ?>>> componentFactories = new HashMap<>();
    private final Map<Class<? extends Entity>, Class<? extends ComponentContainer<?>>> containerClasses = new HashMap<>();
    private final Map<Key, Class<? extends DynamicContainerFactory<?,?>>> factoryClasses = new HashMap<>();

    public boolean requiresStaticFactory(Class<? extends Entity> entityClass) {
        this.ensureInitialized();
        return entityClass == Entity.class || this.componentFactories.containsKey(entityClass);
    }

    public Class<? extends DynamicContainerFactory<?,? extends Component>> spinDedicatedFactory(Key key) {
        this.ensureInitialized();

        // we need a cache as this method is called for a given class each time one of its subclasses is loaded.
        return this.factoryClasses.computeIfAbsent(key, k -> {
            Class<? extends Entity> entityClass = k.entityClass;

            Map<Identifier, EntityComponentFactory<?, ?>> compiled = new LinkedHashMap<>(this.componentFactories.getOrDefault(entityClass, Collections.emptyMap()));
            Class<?> type = entityClass;

            while (type != Entity.class) {
                type = type.getSuperclass();
                this.componentFactories.getOrDefault(type, Collections.emptyMap()).forEach(compiled::putIfAbsent);
            }

            String implSuffix = getSuffix(entityClass);

            try {
                Class<? extends ComponentContainer<?>> containerCls = this.containerClasses.get(entityClass);
                if (containerCls == null) {
                    containerCls = StaticComponentPluginBase.spinComponentContainer(EntityComponentFactory.class, compiled, implSuffix);
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
    public <C extends Component, E extends Entity> void register(ComponentKey<C> type, Class<E> target, EntityComponentFactory<C, E> factory) {
        this.registerFor(target, type, factory);
    }

    @Override
    public <C extends Component, E extends Entity> void registerFor(Class<E> target, ComponentKey<C> type, EntityComponentFactory<C, E> factory) {
        this.checkLoading(EntityComponentFactoryRegistry.class, "register");
        Map<Identifier, EntityComponentFactory<?, ?>> specializedMap = this.componentFactories.computeIfAbsent(target, t -> new HashMap<>());
        EntityComponentFactory<?, ?> previousFactory = specializedMap.get(type.getId());
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + type.getId() + " on " + target + ": " + factory + " and " + previousFactory);
        }
        EntityComponentFactory<Component, E> checked = entity -> Objects.requireNonNull(((EntityComponentFactory<?, E>) factory).createForEntity(entity), "Component factory "+ factory + " for " + type.getId() + " returned null on " + entity.getClass().getSimpleName());
        specializedMap.put(type.getId(), checked);
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
}
