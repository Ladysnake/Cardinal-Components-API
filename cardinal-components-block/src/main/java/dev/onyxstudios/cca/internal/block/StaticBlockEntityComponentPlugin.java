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
package dev.onyxstudios.cca.internal.block;

import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockEntityComponentFactory;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public final class StaticBlockEntityComponentPlugin {
    public static final StaticBlockEntityComponentPlugin INSTANCE = new StaticBlockEntityComponentPlugin();

    private StaticBlockEntityComponentPlugin() {
        super();
    }

    private static String getSuffix(Class<?> entityClass) {
        String simpleName = entityClass.getSimpleName();
        return String.format("BlockEntityImpl_%s_%s", simpleName, Integer.toHexString(entityClass.getName().hashCode()));
    }

    private final Map<Key, Map</*ComponentType*/Identifier, BlockEntityComponentFactory<?, ?>>> componentFactories = new HashMap<>();
    private final Map<Key, Class<? extends DynamicContainerFactory<BlockEntity, Component>>> factoryClasses = new HashMap<>();

    public boolean requiresStaticFactory(Class<? extends BlockEntity> entityClass, @Nullable Direction side) {
        StaticBlockComponentPlugin.INSTANCE.ensureInitialized();
        return entityClass == BlockEntity.class || this.componentFactories.containsKey(new Key(entityClass, side));
    }

    public Class<? extends DynamicContainerFactory<BlockEntity, Component>> spinDedicatedFactory(Key key) {
        StaticBlockComponentPlugin.INSTANCE.ensureInitialized();

        // we need a cache as this method is called for a given class each time one of its subclasses is loaded.
        return this.factoryClasses.computeIfAbsent(key, k -> {
            Class<? extends BlockEntity> entityClass = k.clazz;

            Map<Identifier, BlockEntityComponentFactory<?, ?>> compiled = new LinkedHashMap<>(this.componentFactories.getOrDefault(key, Collections.emptyMap()));
            Class<? extends BlockEntity> type = entityClass;

            while (type != BlockEntity.class) {
                type = type.getSuperclass().asSubclass(BlockEntity.class);
                this.componentFactories.getOrDefault(new Key(type, key.side), Collections.emptyMap()).forEach(compiled::putIfAbsent);
            }

            String implSuffix = getSuffix(entityClass);

            try {
                Class<? extends ComponentContainer<?>> containerCls;

                containerCls = StaticComponentPluginBase.spinComponentContainer(BlockEntityComponentFactory.class, Component.class, compiled, implSuffix);

                return StaticComponentPluginBase.spinContainerFactory(
                    implSuffix + (k.side == null ? "" : "_" + k.side.getName()),
                    DynamicContainerFactory.class,
                    containerCls,
                    null,
                    0,
                    entityClass
                );
            } catch (IOException e) {
                throw new StaticComponentLoadingException("Failed to generate a dedicated component container for " + entityClass, e);
            }
        });
    }

    public <C extends Component, E extends BlockEntity> void registerFor(Class<E> target, Direction side, ComponentKey<C> type, BlockEntityComponentFactory<C, E> factory) {
        StaticBlockComponentPlugin.INSTANCE.checkLoading(BlockComponentFactoryRegistry.class, "register");
        Map<Identifier, BlockEntityComponentFactory<?, ?>> specializedMap = this.componentFactories.computeIfAbsent(new Key(target, side), t -> new HashMap<>());
        BlockEntityComponentFactory<?, ?> previousFactory = specializedMap.get(type.getId());
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + type.getId() + " on " + target + ": " + factory + " and " + previousFactory);
        }
        BlockEntityComponentFactory<Component, E> checked = entity -> Objects.requireNonNull(((BlockEntityComponentFactory<?, E>) factory).create(entity), "Component factory "+ factory + " for " + type.getId() + " returned null on " + entity.getClass().getSimpleName());
        specializedMap.put(type.getId(), checked);
    }

    static final class Key {
        private final Class<? extends BlockEntity> clazz;
        @Nullable
        private final Direction side;

        public Key(Class<? extends BlockEntity> clazz, @Nullable Direction side) {
            this.clazz = clazz;
            this.side = side;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            StaticBlockEntityComponentPlugin.Key key = (StaticBlockEntityComponentPlugin.Key) o;
            return this.clazz.equals(key.clazz) &&
                this.side == key.side;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.clazz, this.side);
        }
    }
}
