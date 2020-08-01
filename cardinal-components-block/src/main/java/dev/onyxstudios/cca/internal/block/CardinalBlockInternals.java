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
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CardinalBlockInternals {
    private static final Map<Key, DynamicContainerFactory<BlockEntity, ?>> containerFactories = new HashMap<>();

    public static ComponentContainer<?> createComponents(BlockEntity blockEntity, @Nullable Direction side) {
        return containerFactories.get(new Key(blockEntity.getClass(), side)).create(blockEntity);
    }

    private static final class Key {
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
            Key key = (Key) o;
            return this.clazz.equals(key.clazz) &&
                this.side == key.side;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.clazz, this.side);
        }
    }
}
