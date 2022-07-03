/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2022 OnyxStudios
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
package dev.onyxstudios.cca.test.block;

import dev.onyxstudios.cca.test.base.TickingTestComponent;
import dev.onyxstudios.cca.test.base.Vita;
import io.github.ladysnake.elmendorf.GameTestUtil;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class CcaBlockTestSuite implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void beSerialize(TestContext ctx) {
        BlockEntity be = Objects.requireNonNull(
            BlockEntityType.END_GATEWAY.instantiate(
                ctx.getAbsolutePos(BlockPos.ORIGIN),
                Blocks.END_GATEWAY.getDefaultState()
            )
        );
        be.getComponent(Vita.KEY).setVitality(42);
        NbtCompound nbt = be.createNbt();
        BlockEntity be1 = Objects.requireNonNull(
            BlockEntityType.END_GATEWAY.instantiate(
                ctx.getAbsolutePos(BlockPos.ORIGIN), Blocks.END_GATEWAY.getDefaultState()
            )
        );
        GameTestUtil.assertTrue("New BlockEntity should have values zeroed", be1.getComponent(Vita.KEY).getVitality() == 0);
        be1.readNbt(nbt);
        GameTestUtil.assertTrue("BlockEntity component data should survive deserialization", be1.getComponent(Vita.KEY).getVitality() == 42);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void beComponentsTick(TestContext ctx) {
        ctx.setBlockState(BlockPos.ORIGIN, Blocks.END_PORTAL);
        ctx.waitAndRun(5, () -> {
            int ticks = Objects.requireNonNull(ctx.getBlockEntity(BlockPos.ORIGIN)).getComponent(TickingTestComponent.KEY).serverTicks();
            GameTestUtil.assertTrue("Component should tick 5 times", ticks == 5);
            ctx.complete();
        });
    }
}
