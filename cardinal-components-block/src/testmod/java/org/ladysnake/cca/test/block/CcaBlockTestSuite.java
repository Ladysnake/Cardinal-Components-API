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
package org.ladysnake.cca.test.block;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import org.ladysnake.cca.test.base.LoadAwareTestComponent;
import org.ladysnake.cca.test.base.TickingTestComponent;
import org.ladysnake.cca.test.base.Vita;

import java.util.Objects;

public class CcaBlockTestSuite {
    @GameTest
    public void beSerialize(GameTestHelper ctx) {
        BlockPos pos = ctx.absolutePos(BlockPos.ZERO);
        BlockEntity be = Objects.requireNonNull(
            BlockEntityType.END_GATEWAY.create(
                pos,
                Blocks.END_GATEWAY.defaultBlockState()
            )
        );
        be.getComponent(Vita.KEY).setVitality(42);
        CompoundTag nbt = be.saveWithoutMetadata(ctx.getLevel().registryAccess());
        BlockEntity be1 = Objects.requireNonNull(
            BlockEntityType.END_GATEWAY.create(
                pos, Blocks.END_GATEWAY.defaultBlockState()
            )
        );
        ctx.assertValueEqual(0, be1.getComponent(Vita.KEY).getVitality(), Component.literal("New BlockEntity should have values zeroed -"));
        readBeData(ctx, be1, nbt);
        ctx.assertValueEqual(42, be1.getComponent(Vita.KEY).getVitality(), Component.literal("BlockEntity component data should survive deserialization -"));
        ctx.succeed();
    }

    private static void readBeData(GameTestHelper ctx, BlockEntity blockEntity, CompoundTag nbt) {
        ProblemReporter.Collector errorReporter = new ProblemReporter.Collector(blockEntity.problemPath());
        blockEntity.loadWithComponents(TagValueInput.create(errorReporter, ctx.getLevel().registryAccess(), nbt));
        if (!errorReporter.isEmpty()) {
            ctx.fail(Component.literal(errorReporter.getTreeReport()));
        }
    }

    @GameTest
    public void canQueryThroughLookup(GameTestHelper ctx) {
        BlockPos pos = ctx.absolutePos(BlockPos.ZERO);
        BlockEntity be = Objects.requireNonNull(
            BlockEntityType.END_GATEWAY.create(
                pos,
                Blocks.END_GATEWAY.defaultBlockState()
            )
        );
        getVita(ctx, pos, be).setVitality(42);
        CompoundTag nbt = be.saveWithoutMetadata(ctx.getLevel().registryAccess());
        BlockEntity be1 = Objects.requireNonNull(
            BlockEntityType.END_GATEWAY.create(
                pos, Blocks.END_GATEWAY.defaultBlockState()
            )
        );
        ctx.assertValueEqual(0, getVita(ctx, pos, be1).getVitality(), Component.literal("New BlockEntity should have values zeroed -"));
        readBeData(ctx, be1, nbt);
        ctx.assertValueEqual(42, getVita(ctx, pos, be1).getVitality(), Component.literal("BlockEntity component data should survive deserialization -"));
        ctx.succeed();
    }

    private static Vita getVita(GameTestHelper ctx, BlockPos pos, BlockEntity be) {
        return Objects.requireNonNull(CcaBlockTestMod.VITA_API_LOOKUP.find(ctx.getLevel(), pos, null, be, Direction.DOWN));
    }

    @GameTest
    public void beComponentsTick(GameTestHelper ctx) {
        BlockPos pos = new BlockPos(1, 1, 1);
        ctx.setBlock(pos, Blocks.END_PORTAL);
        ctx.succeedOnTickWhen(5, () -> {
            int ticks = TickingTestComponent.KEY.get(ctx.getBlockEntity(pos, TheEndPortalBlockEntity.class)).serverTicks();
            ctx.assertValueEqual(5, ticks, Component.literal("Component should tick 5 times -"));
        });
    }

    @GameTest
    public void beComponentsLoadUnload(GameTestHelper ctx) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockEntity firstCommandBlock = new CommandBlockEntity(ctx.absolutePos(pos), Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState());
        ctx.assertValueEqual(
            0, LoadAwareTestComponent.KEY.get(firstCommandBlock).getLoadCounter(),
            Component.literal("Load counter should not be incremented until the block entity joins the world -")
        );
        ctx.setBlock(pos, Blocks.CHAIN_COMMAND_BLOCK);
        BlockEntity commandBlock = ctx.getBlockEntity(pos, CommandBlockEntity.class);
        ctx.assertValueEqual(
            1, LoadAwareTestComponent.KEY.get(commandBlock).getLoadCounter(),
            Component.literal("Load counter should be incremented once when the block entity joins the world -")
        );
        ctx.setBlock(pos, Blocks.AIR);
        ctx.runAfterDelay(1, () -> {
            ctx.assertValueEqual(
                0, LoadAwareTestComponent.KEY.get(commandBlock).getLoadCounter(),
                Component.literal("Load counter should be decremented when the block entity leaves the world -")
            );
            ctx.succeed();
        });
    }
}
