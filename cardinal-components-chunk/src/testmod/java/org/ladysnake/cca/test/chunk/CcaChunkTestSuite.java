/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2026 Ladysnake
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
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.ladysnake.cca.test.base.LoadAwareTestComponent;
import org.ladysnake.cca.test.base.TickingTestComponent;
import org.ladysnake.cca.test.base.Vita;
import org.ladysnake.elmendorf.ElmendorfTestContext;

public class CcaChunkTestSuite {
    @GameTest
    public void chunksSerialize(GameTestHelper ctx) {
        ChunkPos pos = ChunkPos.containing(ctx.absolutePos(new BlockPos(1, 0, 1)));
        ChunkAccess c = new LevelChunk(ctx.getLevel(), pos);
        c.getComponent(Vita.KEY).setVitality(42);
        CompoundTag nbt = SerializableChunkData.copyOf(ctx.getLevel(), c).write();
        ChunkAccess c1 = SerializableChunkData.parse(ctx.getLevel(), ctx.getLevel().palettedContainerFactory(), nbt)
            .read(ctx.getLevel(), ctx.getLevel().getPoiManager(), new RegionStorageInfo("", ctx.getLevel().dimension(), ""), pos);
        ctx.assertValueEqual(42, c1.getComponent(Vita.KEY).getVitality(), Component.literal("Chunk component data should survive deserialization -"));
        ctx.succeed();
    }

    @GameTest
    public void chunksTick(GameTestHelper ctx) {
        ((ElmendorfTestContext) ctx).spawnServerPlayer(0, 0, 0);    // Ensure chunk gets ticked
        int baseTicks = ctx.getLevel().getChunk(ctx.absolutePos(BlockPos.ZERO)).getComponent(TickingTestComponent.KEY).serverTicks();
        ctx.runAfterDelay(5, () -> {
            int ticks = ctx.getLevel().getChunk(ctx.absolutePos(BlockPos.ZERO)).getComponent(TickingTestComponent.KEY).serverTicks();
            ctx.assertValueEqual(5, ticks - baseTicks, Component.literal("Component should tick 5 times -"));
            ctx.succeed();
        });
    }

    @GameTest
    public void chunksLoadUnload(GameTestHelper ctx) {
        ((ElmendorfTestContext) ctx).spawnServerPlayer(0, 0, 0);    // Ensure chunk gets ticked
        ctx.assertValueEqual(
            1,
            ctx.getLevel().getChunk(ctx.absolutePos(BlockPos.ZERO)).getComponent(LoadAwareTestComponent.KEY).getLoadCounter(),
            Component.literal("Load counter should be incremented once when the chunk is added to the world -")
        );
        ctx.succeed();
    }
}
