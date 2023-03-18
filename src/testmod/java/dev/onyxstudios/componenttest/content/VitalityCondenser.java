/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 OnyxStudios
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
package dev.onyxstudios.componenttest.content;

import dev.onyxstudios.cca.test.base.Vita;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

public class VitalityCondenser extends Block {
    public VitalityCondenser(Settings settings) {
        super(settings);
    }

    @SuppressWarnings("deprecation")
    @ApiStatus.OverrideOnly
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        Vita.get(world).transferTo(Vita.get(world.getChunk(pos)), 1);
    }

    @SuppressWarnings("deprecation")
    @ApiStatus.OverrideOnly
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hitInfo) {
        // only on client side, to confirm that sync works
        if (world.isClient) {
            player.sendMessage(Text.translatable("componenttest:action.chunk_vitality",
                Objects.requireNonNull(CardinalComponentsTest.VITA_API_LOOKUP.find(world, pos, state, null, hitInfo.getSide())).getVitality()), true);
        }
        return ActionResult.SUCCESS;
    }
}
