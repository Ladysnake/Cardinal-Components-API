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
package dev.onyxstudios.cca.mixin.block.common;

import dev.onyxstudios.cca.api.v3.block.BlockComponentProvider;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.block.InternalBlockComponentProvider;
import dev.onyxstudios.cca.internal.block.StaticBlockComponentPlugin;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Map;

@Mixin(Block.class)
public abstract class MixinBlock extends AbstractBlock implements InternalBlockComponentProvider {
    @Unique
    private Map<ComponentKey<?>, BlockComponentProvider<?>> containerFactories = null;

    public MixinBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public <C extends Component> C getComponent(ComponentKey<C> key, BlockState state, BlockView world, BlockPos pos, @Nullable Direction side) {
        if (this.containerFactories == null) {
            this.containerFactories = StaticBlockComponentPlugin.INSTANCE.getComponentFactories(Registry.BLOCK.getId((Block) (Object) this));
        }

        @SuppressWarnings("unchecked") BlockComponentProvider<? extends C> factory = (BlockComponentProvider<? extends C>) this.containerFactories.get(key);

        return factory == null ? null : factory.get(state, world, pos, side);
    }
}
