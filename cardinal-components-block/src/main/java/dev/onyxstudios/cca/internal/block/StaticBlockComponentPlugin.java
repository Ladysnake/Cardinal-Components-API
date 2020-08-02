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

import dev.onyxstudios.cca.api.v3.block.*;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.CcaAsmHelper;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class StaticBlockComponentPlugin extends LazyDispatcher implements BlockComponentFactoryRegistry {
    public static final StaticBlockComponentPlugin INSTANCE = new StaticBlockComponentPlugin();
    public static final String WILDCARD_IMPL_SUFFIX = "BlockImpl_All";

    private StaticBlockComponentPlugin() {
        super("creating a BlockEntity");
    }

    private static String getSuffix(Identifier blockId) {
        return "BlockImpl_" + CcaAsmHelper.getJavaIdentifierName(blockId);
    }

    private final Map<Key, Map</*ComponentType*/Identifier, BlockComponentFactory<?>>> componentFactories = new HashMap<>();
    private final Map<Key, Class<? extends BlockComponentContainerFactory>> factoryClasses = new HashMap<>();
    private Class<? extends BlockComponentContainerFactory> wildcardFactoryClass;

    public Class<? extends BlockComponentContainerFactory> getFactoryClass(Identifier blockId, @Nullable Direction side) {
        this.ensureInitialized();
        Class<? extends BlockComponentContainerFactory> specificFactory = this.factoryClasses.get(new Key(blockId, side));
        if (specificFactory != null) {
            return specificFactory;
        }
        assert this.wildcardFactoryClass != null;
        return this.wildcardFactoryClass;
    }

    @Override
    protected void init() {
        StaticComponentPluginBase.processInitializers(
            FabricLoader.getInstance().getEntrypointContainers("cardinal-components-block", BlockComponentInitializer.class),
            initializer -> initializer.registerBlockComponentFactories(this)
        );

        Map<Identifier, BlockComponentFactory<?>> wildcardMap = this.componentFactories.getOrDefault(new Key(null, null), Collections.emptyMap());

        try {
            Class<? extends ComponentContainer<BlockComponent>> containerCls = StaticComponentPluginBase.spinComponentContainer(BlockComponentFactory.class, BlockComponent.class, wildcardMap, WILDCARD_IMPL_SUFFIX);
            this.wildcardFactoryClass = StaticComponentPluginBase.spinContainerFactory(WILDCARD_IMPL_SUFFIX, BlockComponentContainerFactory.class, containerCls, null, 0, BlockState.class, BlockView.class, BlockPos.class);
        } catch (IOException e) {
            throw new StaticComponentLoadingException("Failed to generate the fallback component container for block stacks", e);
        }

        for (Map.Entry<Key, Map<Identifier, BlockComponentFactory<?>>> entry : this.componentFactories.entrySet()) {
            if (entry.getKey().blockId == null) continue;

            try {
                Map<Identifier, BlockComponentFactory<?>> compiled = new HashMap<>(entry.getValue());
                wildcardMap.forEach(compiled::putIfAbsent);
                String implSuffix = getSuffix(entry.getKey().blockId);
                Class<? extends ComponentContainer<BlockComponent>> containerCls = StaticComponentPluginBase.spinComponentContainer(BlockComponentFactory.class, BlockComponent.class, compiled, implSuffix);
                this.factoryClasses.put(entry.getKey(), StaticComponentPluginBase.spinContainerFactory(implSuffix, BlockComponentContainerFactory.class, containerCls, null, 0, BlockState.class, BlockView.class, BlockPos.class));
            } catch (IOException e) {
                throw new StaticComponentLoadingException("Failed to generate a dedicated component container for " + entry.getKey().blockId, e);
            }
        }
    }

    @Override
    public <C extends Component, BE extends BlockEntity> void registerFor(Class<BE> target, Direction side, ComponentKey<C> key, BlockEntityComponentFactory<C, BE> factory) {
        StaticBlockEntityComponentPlugin.INSTANCE.registerFor(target, side, key, factory);
    }

    @Override
    public <C extends Component> void registerFor(@Nullable Identifier blockId, @Nullable Direction side, ComponentKey<? super C> type, BlockComponentFactory<C> factory) {
        this.checkLoading(BlockComponentFactoryRegistry.class, "register");
        Map<Identifier, BlockComponentFactory<?>> specializedMap = this.componentFactories.computeIfAbsent(new Key(blockId, side), t -> new HashMap<>());
        BlockComponentFactory<?> previousFactory = specializedMap.get(type.getId());
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + type.getId() + " on " + (blockId == null ? "every block" : "block '" + blockId + "'") + ": " + factory + " and " + previousFactory);
        }
        specializedMap.put(type.getId(), factory);
    }

    private static final class Key {
        @Nullable
        private final Identifier blockId;
        @Nullable
        private final Direction side;

        public Key(@Nullable Identifier blockId, @Nullable Direction side) {
            this.blockId = blockId;
            this.side = side;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(this.blockId, key.blockId) &&
                this.side == key.side;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.blockId, this.side);
        }
    }
}
