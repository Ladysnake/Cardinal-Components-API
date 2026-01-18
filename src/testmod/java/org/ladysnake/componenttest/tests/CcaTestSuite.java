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
package org.ladysnake.componenttest.tests;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.ladysnake.cca.api.v3.component.ComponentAccess;

public final class CcaTestSuite {
    @GameTest
    public void interfacesGetInjected(GameTestHelper context) {
        // CCA Block
        context.setBlock(1, 0, 2, Blocks.CHEST);
        checkContainer(context, context.getBlockEntity(new BlockPos(1, 0, 2), ChestBlockEntity.class));
        // CCA Chunk
        checkContainer(context, context.getLevel().getChunk(context.absolutePos(BlockPos.ZERO)));
        // CCA Entity
        checkContainer(context, context.spawnWithNoFreeWill(EntityType.AXOLOTL, 1, 0, 1));
        // CCA Level
        checkContainer(context, context.getLevel().getLevelData());
        // CCA Scoreboard
        checkContainer(context, context.getLevel().getScoreboard());
        checkContainer(context, context.getLevel().getScoreboard().addPlayerTeam("testX"));
        context.succeed();
    }

    private void checkContainer(GameTestHelper ctx, Object provider) {
        //noinspection ConstantConditions
        ctx.assertTrue(provider + " should correctly implement ComponentProvider", ((ComponentAccess) provider).asComponentProvider().getComponentContainer() != null);
    }
}
