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
package dev.onyxstudios.cca.internal.block;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import net.minecraft.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

public final class CardinalBlockInternals {
    private static final Map<Class<? extends BlockEntity>, ComponentContainer.Factory<BlockEntity>> entityContainerFactories = new HashMap<>();
    private static final Object factoryMutex = new Object();

    public static ComponentContainer createComponents(BlockEntity blockEntity) {
        Class<? extends BlockEntity> entityClass = blockEntity.getClass();
        ComponentContainer.Factory<BlockEntity> existing = entityContainerFactories.get(entityClass);

        if (existing != null) {
            return existing.createContainer(blockEntity);
        }

        synchronized (factoryMutex) {   // can be called from both client and server thread
            // computeIfAbsent and not put, because the factory may have been generated while waiting
            return entityContainerFactories.computeIfAbsent(entityClass, clazz -> {
                Class<?> cl = clazz;
                Class<? extends BlockEntity> parentWithStaticComponents = null;

                while (BlockEntity.class.isAssignableFrom(cl)) {
                    Class<? extends BlockEntity> c = cl.asSubclass(BlockEntity.class);

                    if (parentWithStaticComponents == null && StaticBlockComponentPlugin.INSTANCE.requiresStaticFactory(c)) {   // try to find a specialized ASM factory
                        parentWithStaticComponents = c;
                    }
                    cl = c.getSuperclass();
                }
                assert parentWithStaticComponents != null;
                Class<? extends ComponentContainer.Factory<BlockEntity>> factoryClass = StaticBlockComponentPlugin.INSTANCE.spinDedicatedFactory(parentWithStaticComponents);

                return ComponentsInternals.createFactory(factoryClass);
            }).createContainer(blockEntity);
        }
    }
}
