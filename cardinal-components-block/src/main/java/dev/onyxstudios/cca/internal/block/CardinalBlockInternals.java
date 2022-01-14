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
import net.minecraft.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CardinalBlockInternals {
    private static final Map<Class<? extends BlockEntity>, ComponentContainer.Factory<BlockEntity>> entityContainerFactories = new HashMap<>();

    public static ComponentContainer createComponents(BlockEntity blockEntity) {
        Class<? extends BlockEntity> entityClass = blockEntity.getClass();
        ComponentContainer.Factory<BlockEntity> existing = entityContainerFactories.get(entityClass);

        return Objects.requireNonNullElseGet(
            existing,
            () -> getBeComponentFactory(entityClass)
        ).createContainer(blockEntity);
    }

    private static synchronized ComponentContainer.Factory<BlockEntity> getBeComponentFactory(Class<? extends BlockEntity> entityClass) {
        // need to check again despite synchronization, because
        // 1- recursive calls
        // 2- the factory may have been generated while waiting from createComponents
        ComponentContainer.Factory<BlockEntity> existing = entityContainerFactories.get(entityClass);
        if (existing != null) return existing;

        ComponentContainer.Factory<BlockEntity> factory;
        if (StaticBlockComponentPlugin.INSTANCE.requiresStaticFactory(entityClass)) {
            factory = StaticBlockComponentPlugin.INSTANCE.buildDedicatedFactory(entityClass);
        } else {
            @SuppressWarnings("unchecked") var superclass = (Class<? extends BlockEntity>) entityClass.getSuperclass();
            assert BlockEntity.class.isAssignableFrom(superclass) : "requiresStaticFactory returned false on BlockEntity?";
            factory = /* recursive call */ getBeComponentFactory(superclass);
        }
        entityContainerFactories.put(entityClass, factory);
        return factory;
    }
}
