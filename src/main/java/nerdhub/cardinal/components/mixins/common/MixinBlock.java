/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
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
package nerdhub.cardinal.components.mixins.common;

import com.google.common.collect.ImmutableSet;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.BlockComponentProvider;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(Block.class)
public class MixinBlock implements BlockComponentProvider {

    @Override
    public <T extends Component> boolean hasComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof BlockComponentProvider && ((BlockComponentProvider) be).hasComponent(blockView, pos, type, side);
    }

    @Nullable
    @Override
    public <T extends Component> T getComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof BlockComponentProvider ? ((BlockComponentProvider) be).getComponent(blockView, pos, type, side) : null;
    }

    @Override
    public Set<ComponentType<? extends Component>> getComponentTypes(BlockView blockView, BlockPos pos, @Nullable Direction side) {
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof BlockComponentProvider ? ((BlockComponentProvider) be).getComponentTypes(blockView, pos, side) : ImmutableSet.of();
    }
}
