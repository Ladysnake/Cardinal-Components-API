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
package dev.onyxstudios.cca.api.v3.block;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

/**
 * A component provider for blocks in a World.
 *
 * <p>When invoked, the factory must return a {@link Component} of the right type.
 * Unlike traditional component factories, implementations may return an existing component instance,
 * possibly attached to another provider.
 *
 * <p>A custom implementation of this interface may be combined with a registered {@link BlockEntityComponentFactory}
 * to provide different components based on the queried side:
 * <pre>{@code (state, world, pos, side) -> {
 *   Optional<MySidedComponent> sided = SIDED.maybeGet(world.getBlockEntity(pos));
 *   if (sided.isPresent()) {
 *      return sided.get().getForSide(side);
 *   }
 *   return null;
 * }
 * }</pre>
 * Where {@code SIDED} is an internal {@link ComponentKey} that has been
 * {@link BlockComponentFactoryRegistry#registerFor(Class, ComponentKey, BlockEntityComponentFactory) registered}
 * for the {@link BlockEntity} normally associated with the block.
 *
 * @see BlockComponents
 * @see BlockComponentFactoryRegistry
 * @since 2.5.0
 */
@FunctionalInterface
public interface BlockComponentProvider<C extends Component> {
    /**
     * Retrieve a component instance from the given context
     *
     * @param state the state of the block being queried
     * @param world the block context, which may or may not be a fully fledged {@link net.minecraft.world.World}
     * @param pos   the position at which the block is placed
     * @param side  side of the block being specifically targeted
     * @return a component instance if applicable, or {@code null}
     */
    @Nullable C get(BlockState state, BlockView world, BlockPos pos, @Nullable Direction side);
}
