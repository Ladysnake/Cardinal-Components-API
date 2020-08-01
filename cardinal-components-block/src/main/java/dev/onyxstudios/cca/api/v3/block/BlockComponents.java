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
package dev.onyxstudios.cca.api.v3.block;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.block.BlockComponentProvider;
import dev.onyxstudios.cca.internal.block.BlockEntityComponentProvider;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class BlockComponents {

    public static <C extends Component> @Nullable C get(ComponentKey<C> key, BlockEntity blockEntity) {
        return get(key, blockEntity, null);
    }

    public static <C extends Component> @Nullable C get(ComponentKey<C> key, BlockEntity blockEntity, @Nullable Direction side) {
        World world = blockEntity.getWorld();

        if (world != null) {
            @Nullable C res = getFromBlock(key, world, blockEntity.getPos(), side, blockEntity.getCachedState());

            if (res != null) {
                return res;
            }
        }

        return getFromBlockEntity(key, blockEntity, side);
    }

    public static <C extends Component> @Nullable C get(ComponentKey<C> key, BlockView world, BlockPos pos) {
        return get(key, world, pos, null);
    }

    public static <C extends Component> @Nullable C get(ComponentKey<C> key, BlockState blockState, BlockView world, BlockPos pos) {
        return get(key, blockState, world, pos, null);
    }

    public static <C extends Component> @Nullable C get(ComponentKey<C> key, BlockView world, BlockPos pos, @Nullable Direction side) {
        return get(key, world.getBlockState(pos), world, pos, side);
    }

    public static <C extends Component> @Nullable C get(ComponentKey<C> key, BlockState blockState, BlockView world, BlockPos pos, @Nullable Direction side) {
        @Nullable C res = getFromBlock(key, world, pos, side, blockState);

        if (res != null) {
            return res;
        }

        return getFromBlockEntity(key, world.getBlockEntity(pos), side);
    }

    private static <C extends Component> @Nullable C getFromBlockEntity(ComponentKey<C> key, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        BlockEntityComponentProvider be = (BlockEntityComponentProvider) blockEntity;

        if (be != null) {
            if (side != null) {
                C res = key.getInternal(be.getComponentContainer(side));

                if (res != null) {
                    return res;
                }
            }

            return key.getInternal(be.getComponentContainer(null));
        }
        return null;
    }

    private static <C extends Component> @Nullable C getFromBlock(ComponentKey<C> key, BlockView world, BlockPos pos, @Nullable Direction side, BlockState state) {
        BlockComponentProvider blockProvider = ((BlockComponentProvider) state.getBlock());
        ComponentContainer<?> components = blockProvider.getComponents(state, world, pos, side);
        return key.getInternal(components);
    }
}
