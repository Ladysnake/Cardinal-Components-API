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
package dev.onyxstudios.cca.api.v3.block.util;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A sided compound component container that uses a supplier to obtain
 * its side components
 */
public final class SuppliedSidedContainerCompound implements SidedContainerCompound {
    private final Map<Direction, ComponentContainer> sides = new EnumMap<>(Direction.class);
    private final Supplier<ComponentContainer> factory;
    private ComponentContainer core;

    public SuppliedSidedContainerCompound(Supplier<ComponentContainer> factory) {
        this.factory = factory;
    }

    @Override
    public ComponentContainer get(@Nullable Direction side) {
        if (side == null) {
            return core == null ? (core = factory.get()) : core;
        }
        return sides.computeIfAbsent(side, d -> factory.get());
    }

    @Override
    public void fromTag(CompoundTag serialized) {
        sides.forEach((direction, componentContainer) -> componentContainer.fromTag(serialized.getCompound(direction.name())));
        core.fromTag(serialized.getCompound("core"));
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        sides.forEach((direction, componentContainer) -> tag.put(direction.name(), componentContainer.toTag(new CompoundTag())));
        tag.put("core", core.toTag(new CompoundTag()));
        return tag;
    }
}
