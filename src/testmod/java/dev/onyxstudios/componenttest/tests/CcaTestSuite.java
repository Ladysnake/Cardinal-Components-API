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
package dev.onyxstudios.componenttest.tests;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.ladysnake.elmendorf.GameTestUtil;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public final class CcaTestSuite implements FabricGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void interfacesGetInjected(TestContext context) {
        // CCA Block
        context.setBlockState(1, 0, 2, Blocks.CHEST);
        checkContainer(Objects.requireNonNull(context.getBlockEntity(new BlockPos(1, 0, 2))));
        // CCA Chunk
        checkContainer(context.getWorld().getChunk(context.getAbsolutePos(BlockPos.ORIGIN)));
        // CCA Entity
        checkContainer(context.<AxolotlEntity>spawnMob(EntityType.AXOLOTL, 1, 0, 1));
        // CCA Level
        checkContainer(context.getWorld().getLevelProperties());
        // CCA Scoreboard
        checkContainer(context.getWorld().getScoreboard());
        checkContainer(context.getWorld().getScoreboard().addTeam("testX"));
        context.complete();
    }

    private void checkContainer(ComponentProvider provider) {
        //noinspection ConstantConditions
        GameTestUtil.assertTrue(provider + " should correctly implement ComponentProvider", provider.getComponentContainer() != null);
    }
}
