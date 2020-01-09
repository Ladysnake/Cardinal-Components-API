/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 GlassPane
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
package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

public interface BlockComponentProvider {

    static BlockComponentProvider get(BlockState state) {
        return get(state.getBlock());
    }

    static BlockComponentProvider get(Block block) {
        return (BlockComponentProvider) block;
    }

    /**
     * if this method returns {@code true}, then {@link #getComponent(BlockView, BlockPos, ComponentType, Direction)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     *
     * @return whether or not this {@link BlockComponentProvider} can provide the desired component
     */
    <T extends Component> boolean hasComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side);

    /**
     * @return an instance of the requested component, or {@code null}
     */
    @Nullable
    <T extends Component> T getComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side);

    default <T extends Component> Optional<T> optionally(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        return Optional.ofNullable(getComponent(blockView, pos, type, side));
    }

    /**
     * @return an <strong>immutable</strong> view of the component types exposed by this {@link BlockComponentProvider}
     */
    Set<ComponentType<?>> getComponentTypes(BlockView blockView, BlockPos pos, @Nullable Direction side);
}
