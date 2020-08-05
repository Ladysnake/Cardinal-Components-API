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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.BlockComponentProvider;
import nerdhub.cardinal.components.api.component.Component;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class CompoundBlockComponentProvider implements BlockComponentProvider {
    private final BlockComponentProvider primary;
    private final List<BlockComponentProvider> secondary;

    public CompoundBlockComponentProvider(BlockComponentProvider primary) {
        this.primary = primary;
        this.secondary = new ArrayList<>();
    }

    public void addSecondary(BlockComponentProvider provider) {
        secondary.add(provider);
    }

    @Override
    public <T extends Component> boolean hasComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        if (primary.hasComponent(blockView, pos, type, side)) return true;
        for (BlockComponentProvider provider : secondary) {
            if (provider.hasComponent(blockView, pos, type, side)) return true;
        }
        return false;
    }

    @Nullable
    @Override
    public <T extends Component> T getComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        T potential = primary.getComponent(blockView, pos, type, side);
        if (potential != null) return potential;
        for (BlockComponentProvider provider : secondary) {
            potential = provider.getComponent(blockView, pos, type, side);
            if (potential != null) return potential;
        }
        return null;
    }

    @Override
    public Set<ComponentType<?>> getComponentTypes(BlockView blockView, BlockPos pos, @Nullable Direction side) {
        Set<ComponentType<?>> ret = new HashSet<>(primary.getComponentTypes(blockView, pos, side));
        for (BlockComponentProvider provider : secondary) {
            ret.addAll(provider.getComponentTypes(blockView, pos, side));
        }
        return ret;
    }
}
