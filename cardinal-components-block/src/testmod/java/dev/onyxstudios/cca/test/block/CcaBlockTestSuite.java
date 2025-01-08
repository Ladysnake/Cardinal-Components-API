/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2025 OnyxStudios
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

import dev.onyxstudios.cca.internal.block.StaticBlockComponentPlugin;
import dev.onyxstudios.cca.test.base.LoadAwareTestComponent;
import dev.onyxstudios.cca.test.base.TickingTestComponent;
import dev.onyxstudios.cca.test.base.Vita;
import io.github.ladysnake.elmendorf.GameTestUtil;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

public class CcaBlockTestSuite implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE) public void beSerialize(TestContext ctx) {
        BlockPos pos = ctx.getAbsolutePos(BlockPos.ORIGIN);
        BlockEntity be = Objects.requireNonNull(BlockEntityType.END_GATEWAY.instantiate(pos, Blocks.END_GATEWAY.getDefaultState()));
        be.getComponent(Vita.KEY).setVitality(42);
        NbtCompound nbt = be.createNbt();
        BlockEntity be1 = Objects.requireNonNull(BlockEntityType.END_GATEWAY.instantiate(pos, Blocks.END_GATEWAY.getDefaultState()));
        GameTestUtil.assertTrue("New BlockEntity should have values zeroed", be1.getComponent(Vita.KEY).getVitality() == 0);
        be1.readNbt(nbt);
        GameTestUtil.assertTrue("BlockEntity component data should survive deserialization", be1.getComponent(Vita.KEY).getVitality() == 42);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE) public void canQueryThroughLookup(TestContext ctx) {
        BlockPos pos = ctx.getAbsolutePos(BlockPos.ORIGIN);
        BlockEntity be = Objects.requireNonNull(BlockEntityType.END_GATEWAY.instantiate(pos, Blocks.END_GATEWAY.getDefaultState()));
        getVita(ctx, pos, be).setVitality(42);
        NbtCompound nbt = be.createNbt();
        BlockEntity be1 = Objects.requireNonNull(BlockEntityType.END_GATEWAY.instantiate(pos, Blocks.END_GATEWAY.getDefaultState()));
        GameTestUtil.assertTrue("New BlockEntity should have values zeroed", getVita(ctx, pos, be1).getVitality() == 0);
        be1.readNbt(nbt);
        GameTestUtil.assertTrue("BlockEntity component data should survive deserialization", getVita(ctx, pos, be1).getVitality() == 42);
        ctx.complete();
    }

    @NotNull private static Vita getVita(TestContext ctx, BlockPos pos, BlockEntity be) {
        return Objects.requireNonNull(CcaBlockTestMod.VITA_API_LOOKUP.find(ctx.getWorld(), pos, null, be, Direction.DOWN));
    }

    @GameTest(templateName = EMPTY_STRUCTURE) public void beComponentsTick(TestContext ctx) {
        ctx.setBlockState(BlockPos.ORIGIN, Blocks.END_PORTAL);

        var blockentity = ctx.getBlockEntity(BlockPos.ORIGIN);
        GameTestUtil.assertTrue("Block entity should not be null", blockentity != null);
        GameTestUtil.assertTrue("BlockEntity should have TickingTestComponent", TickingTestComponent.KEY.getNullable(blockentity) != null);
        GameTestUtil.assertTrue("Class should be registered as server ticker", StaticBlockComponentPlugin.INSTANCE.serverTicking.contains(blockentity.getClass()));
        GameTestUtil.assertTrue("Class should be registered as client ticker", StaticBlockComponentPlugin.INSTANCE.clientTicking.contains(blockentity.getClass()));

        ctx.waitAndRun(5, () -> {
            var blockentity2 = ctx.getBlockEntity(BlockPos.ORIGIN);
            GameTestUtil.assertTrue("Block entity should still exist", blockentity2 != null);
            int ticks = blockentity2.getComponent(TickingTestComponent.KEY).serverTicks();
            GameTestUtil.assertTrue("Component should tick 5 times", ticks == 5);
            ctx.complete();
        });
    }

    @GameTest(templateName = EMPTY_STRUCTURE) public void beComponentsLoadUnload(TestContext ctx) {
        BlockEntity firstCommandBlock = new CommandBlockBlockEntity(ctx.getAbsolutePos(BlockPos.ORIGIN), Blocks.CHAIN_COMMAND_BLOCK.getDefaultState());
        GameTestUtil.assertTrue("Load counter should not be incremented until the block entity joins the world", LoadAwareTestComponent.KEY.get(firstCommandBlock).getLoadCounter() == 0);
        ctx.setBlockState(BlockPos.ORIGIN, Blocks.CHAIN_COMMAND_BLOCK);
        BlockEntity commandBlock = Objects.requireNonNull(ctx.getBlockEntity(BlockPos.ORIGIN));
        GameTestUtil.assertTrue("Load counter should be incremented once when the block entity joins the world", LoadAwareTestComponent.KEY.get(commandBlock).getLoadCounter() == 1);
        ctx.setBlockState(BlockPos.ORIGIN, Blocks.AIR);
        ctx.waitAndRun(1, () -> {
            GameTestUtil.assertTrue("Load counter should be decremented when the block entity leaves the world", LoadAwareTestComponent.KEY.get(commandBlock).getLoadCounter() == 0);
            ctx.complete();
        });
    }

    @GameTest(templateName = EMPTY_STRUCTURE) public void rootClassServerTicker(TestContext ctx) {
        ctx.setBlockState(BlockPos.ORIGIN, Blocks.BARREL);

        var blockentity = ctx.getBlockEntity(BlockPos.ORIGIN);
        GameTestUtil.assertTrue("Block entity should not be null", blockentity != null);

        GameTestUtil.assertTrue("Class should be registered as server ticker", StaticBlockComponentPlugin.INSTANCE.serverTicking.contains(blockentity.getClass()));
        GameTestUtil.assertFalse("Class should NOT be registered as client ticker", StaticBlockComponentPlugin.INSTANCE.clientTicking.contains(blockentity.getClass()));

        var component = GlobalTickingComponent.KEY.getNullable(blockentity);
        GameTestUtil.assertTrue("Component should exist", component != null);

        AtomicInteger flag = new AtomicInteger(0);
        BooleanSupplier action = () -> {
            flag.getAndIncrement();
            return false;
        };
        component.setTickAction(action);
        GameTestUtil.assertTrue("Tick action should be set", component.getTickAction().isPresent());

        ctx.waitAndRun(5, () -> {
            var blockentity2 = ctx.getBlockEntity(BlockPos.ORIGIN);
            GameTestUtil.assertTrue("Block entity should still exist", blockentity2 != null);
            GameTestUtil.assertTrue("Tick action should be cleared", blockentity2.getComponent(GlobalTickingComponent.KEY).getTickAction().isEmpty());
            GameTestUtil.assertTrue("Tick action should have run exactly once", flag.get() == 1);

            ctx.complete();
        });
    }

    /**
     * same as {@link CcaBlockTestSuite#rootClassServerTicker(TestContext)} but for a BlockEntity that has an explicit
     * component registered, so that {@link StaticBlockComponentPlugin#requiresStaticFactory(Class)} returns true for
     * the class itself rather than delegating to the parent class.
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void rootClassServerTickerWithExplicitRegistration(TestContext ctx) {
        ctx.setBlockState(BlockPos.ORIGIN, Blocks.COMMAND_BLOCK);

        var blockentity = ctx.getBlockEntity(BlockPos.ORIGIN);
        GameTestUtil.assertTrue("Block entity should not be null", blockentity != null);
        GameTestUtil.assertTrue("Class should be registered as server ticker", StaticBlockComponentPlugin.INSTANCE.serverTicking.contains(blockentity.getClass()));

        var component = GlobalTickingComponent.KEY.getNullable(blockentity);
        GameTestUtil.assertTrue("Component should exist", component != null);

        ctx.complete();
    }
}
