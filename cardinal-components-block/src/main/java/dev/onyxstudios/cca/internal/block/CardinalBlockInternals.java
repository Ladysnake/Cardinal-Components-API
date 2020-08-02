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

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class CardinalBlockInternals {
    private static final Map<Class<? extends BlockEntity>, Map<Direction, DynamicContainerFactory<BlockEntity, Component>>> entityContainerFactories = new HashMap<>();
    private static final Object factoryMutex = new Object();

    public static ComponentContainer<?> createComponents(BlockEntity blockEntity, @Nullable Direction side) {
        Class<? extends BlockEntity> entityClass = blockEntity.getClass();
        Map<Direction, DynamicContainerFactory<BlockEntity, Component>> sided = entityContainerFactories.computeIfAbsent(entityClass, k -> new Reference2ObjectOpenHashMap<>());
        DynamicContainerFactory<BlockEntity, Component> existing = sided.get(side);
        if (existing != null) {
            return existing.create(blockEntity);
        }
        synchronized (factoryMutex) {
            // computeIfAbsent and not put, because the factory may have been generated while waiting
            return sided.computeIfAbsent(side, s -> {
                Class<?> cl = entityClass;
                Class<? extends BlockEntity> parentWithStaticComponents = null;

                while (BlockEntity.class.isAssignableFrom(cl)) {
                    Class<? extends BlockEntity> c = cl.asSubclass(BlockEntity.class);
                    if (parentWithStaticComponents == null && StaticBlockEntityComponentPlugin.INSTANCE.requiresStaticFactory(c, s)) {   // try to find a specialized ASM factory
                        parentWithStaticComponents = c;
                    }
                    cl = c.getSuperclass();
                }
                assert parentWithStaticComponents != null;
                Class<? extends DynamicContainerFactory<BlockEntity,Component>> factoryClass = StaticBlockEntityComponentPlugin.INSTANCE.spinDedicatedFactory(
                    new StaticBlockEntityComponentPlugin.Key(parentWithStaticComponents, s)
                );

                return ComponentsInternals.createFactory(factoryClass);
            }).create(blockEntity);
        }
    }

    public static BlockComponentContainerFactory createBlockContainerFactory(Block block, @Nullable Direction side) {
        Identifier blockId = Registry.BLOCK.getId(block);
        return ComponentsInternals.createFactory(StaticBlockComponentPlugin.INSTANCE.getFactoryClass(blockId, side));
    }
}
