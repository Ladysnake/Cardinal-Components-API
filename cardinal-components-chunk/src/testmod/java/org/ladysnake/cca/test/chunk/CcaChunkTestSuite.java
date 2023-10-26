/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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
package org.ladysnake.cca.test.chunk;

import org.ladysnake.cca.test.base.LoadAwareTestComponent;
import org.ladysnake.cca.test.base.TickingTestComponent;
import org.ladysnake.cca.test.base.Vita;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.ladysnake.elmendorf.GameTestUtil;

public class CcaChunkTestSuite implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void chunksSerialize(TestContext ctx) {
        ChunkPos pos = new ChunkPos(ctx.getAbsolutePos(new BlockPos(1, 0, 1)));
        Chunk c = new WorldChunk(ctx.getWorld(), pos);
        c.getComponent(Vita.KEY).setVitality(42);
        NbtCompound nbt = ChunkSerializer.serialize(ctx.getWorld(), c);
        Chunk c1 = ChunkSerializer.deserialize(ctx.getWorld(), ctx.getWorld().getPointOfInterestStorage(), pos, nbt);
        GameTestUtil.assertTrue("Chunk component data should survive deserialization", c1.getComponent(Vita.KEY).getVitality() == 42);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void chunksTick(TestContext ctx) {
        ctx.spawnServerPlayer(0, 0, 0);    // Ensure chunk gets ticked
        int baseTicks = ctx.getWorld().getChunk(ctx.getAbsolutePos(BlockPos.ORIGIN)).getComponent(TickingTestComponent.KEY).serverTicks();
        ctx.waitAndRun(5, () -> {
            int ticks = ctx.getWorld().getChunk(ctx.getAbsolutePos(BlockPos.ORIGIN)).getComponent(TickingTestComponent.KEY).serverTicks();
            GameTestUtil.assertTrue("Component should tick 5 times", ticks - baseTicks == 5);
            ctx.complete();
        });
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void chunksLoadUnload(TestContext ctx) {
        ctx.spawnServerPlayer(0, 0, 0);    // Ensure chunk gets ticked
        GameTestUtil.assertTrue(
            "Load counter should be incremented once when the chunk is added to the world",
            ctx.getWorld().getChunk(ctx.getAbsolutePos(BlockPos.ORIGIN)).getComponent(LoadAwareTestComponent.KEY).getLoadCounter() == 1
        );
        ctx.complete();
    }
}
