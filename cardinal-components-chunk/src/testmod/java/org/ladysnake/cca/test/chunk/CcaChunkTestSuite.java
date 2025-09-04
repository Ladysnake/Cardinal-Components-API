/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2025 Ladysnake
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

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.SerializedChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.storage.StorageKey;
import org.ladysnake.cca.test.base.LoadAwareTestComponent;
import org.ladysnake.cca.test.base.TickingTestComponent;
import org.ladysnake.cca.test.base.Vita;

public class CcaChunkTestSuite {
    @GameTest
    public void chunksSerialize(TestContext ctx) {
        ChunkPos pos = new ChunkPos(ctx.getAbsolutePos(new BlockPos(1, 0, 1)));
        Chunk c = new WorldChunk(ctx.getWorld(), pos);
        c.getComponent(Vita.KEY).setVitality(42);
        NbtCompound nbt = SerializedChunk.fromChunk(ctx.getWorld(), c).serialize();
        Chunk c1 = SerializedChunk.fromNbt(ctx.getWorld(), ctx.getWorld().getPalettesFactory(), nbt)
            .convert(ctx.getWorld(), ctx.getWorld().getPointOfInterestStorage(), new StorageKey("", ctx.getWorld().getRegistryKey(), ""), pos);
        ctx.assertEquals(42, c1.getComponent(Vita.KEY).getVitality(), Text.literal("Chunk component data should survive deserialization -"));
        ctx.complete();
    }

    @GameTest
    public void chunksTick(TestContext ctx) {
        ctx.spawnServerPlayer(0, 0, 0);    // Ensure chunk gets ticked
        int baseTicks = ctx.getWorld().getChunk(ctx.getAbsolutePos(BlockPos.ORIGIN)).getComponent(TickingTestComponent.KEY).serverTicks();
        ctx.waitAndRun(5, () -> {
            int ticks = ctx.getWorld().getChunk(ctx.getAbsolutePos(BlockPos.ORIGIN)).getComponent(TickingTestComponent.KEY).serverTicks();
            ctx.assertEquals(5, ticks - baseTicks, Text.literal("Component should tick 5 times -"));
            ctx.complete();
        });
    }

    @GameTest
    public void chunksLoadUnload(TestContext ctx) {
        ctx.spawnServerPlayer(0, 0, 0);    // Ensure chunk gets ticked
        ctx.assertEquals(
            1,
            ctx.getWorld().getChunk(ctx.getAbsolutePos(BlockPos.ORIGIN)).getComponent(LoadAwareTestComponent.KEY).getLoadCounter(),
            Text.literal("Load counter should be incremented once when the chunk is added to the world -")
        );
        ctx.complete();
    }
}
