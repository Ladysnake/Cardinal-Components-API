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
package dev.onyxstudios.cca.mixin.block.common;

import com.google.common.collect.ImmutableSet;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.BlockComponentProvider;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

@Mixin(AbstractBlock.class)
public abstract class MixinBlock implements dev.onyxstudios.cca.internal.block.BlockComponentProvider, BlockComponentProvider {
    private static final ComponentContainer<?> EMPTY_CONTAINER = ComponentContainer.factoryBuilder(Object.class).build().apply(null);

    @Shadow public abstract boolean hasBlockEntity();

    @Override
    public ComponentContainer<?> getComponents(BlockState state, BlockView world, BlockPos pos, @Nullable Direction side) {
        // TODO
        return EMPTY_CONTAINER;
    }

    @Override
    public <T extends Component> boolean hasComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        if (!this.hasBlockEntity()) return false;
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof BlockComponentProvider && ((BlockComponentProvider) be).hasComponent(blockView, pos, type, side);
    }

    @Nullable
    @Override
    public <T extends Component> T getComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        if (!this.hasBlockEntity()) return null;
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof BlockComponentProvider ? ((BlockComponentProvider) be).getComponent(blockView, pos, type, side) : null;
    }

    @Override
    public Set<ComponentType<?>> getComponentTypes(BlockView blockView, BlockPos pos, @Nullable Direction side) {
        if (!this.hasBlockEntity()) return Collections.emptySet();
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof BlockComponentProvider ? ((BlockComponentProvider) be).getComponentTypes(blockView, pos, side) : ImmutableSet.of();
    }
}
