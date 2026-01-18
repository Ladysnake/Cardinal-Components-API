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
package org.ladysnake.cca.test.entity;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.ladysnake.cca.test.base.LoadAwareTestComponent;
import org.ladysnake.cca.test.base.Vita;

public class CcaEntityTestSuite {
    @GameTest
    public void bucketableWorks(GameTestHelper ctx) {
        ServerPlayer player = ctx.spawnServerPlayer(1, 0, 1);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        BlockPos pos = new BlockPos(2, 0, 2);
        var axolotl = ctx.spawnWithNoFreeWill(EntityType.AXOLOTL, pos);
        axolotl.getComponent(Vita.KEY).setVitality(3);
        Bucketable.bucketMobPickup(player, InteractionHand.MAIN_HAND, axolotl);
        ((MobBucketItem) Items.AXOLOTL_BUCKET).checkExtraContent(player, ctx.getLevel(), player.getItemInHand(InteractionHand.MAIN_HAND), ctx.absolutePos(pos));
        ctx.succeedWhenEntityData(pos, EntityType.AXOLOTL, a -> a.getComponent(Vita.KEY).getVitality(), 3);
    }

    @GameTest
    public void loadEventsWork(GameTestHelper ctx) {
        Shulker shulker = new Shulker(EntityType.SHULKER, ctx.getLevel());
        Vec3 vec3d = ctx.absoluteVec(new Vec3(1, 0, 1));
        shulker.snapTo(vec3d.x, vec3d.y, vec3d.z, shulker.getYRot(), shulker.getXRot());
        ctx.assertValueEqual(
            0, LoadAwareTestComponent.KEY.get(shulker).getLoadCounter(),
            Component.literal("Load counter should not be incremented until the entity joins the world -")
        );
        ctx.getLevel().addFreshEntity(shulker);
        ctx.assertValueEqual(
            1, LoadAwareTestComponent.KEY.get(shulker).getLoadCounter(),
            Component.literal("Load counter should be incremented once when the entity joins the world -")
        );
        shulker.remove(Entity.RemovalReason.DISCARDED);
        ctx.runAfterDelay(1, () -> {
            ctx.assertValueEqual(
                0,
                LoadAwareTestComponent.KEY.get(shulker).getLoadCounter(),
                Component.literal("Load counter should be decremented when the entity leaves the world -")
            );
            ctx.succeed();
        });
    }

    @GameTest
    public void moddedEntitiesWork(GameTestHelper ctx) {
        ctx.spawn(CcaEntityTestMod.TEST_ENTITY, 0, 0, 0);
        ctx.succeed();
    }

    @GameTest
    public void respawnHappensOnConversion(GameTestHelper ctx) {
        Camel camel = ctx.spawn(EntityType.CAMEL, 0, 0, 0);
        Cow cow = camel.convertTo(EntityType.COW, ConversionParams.single(camel, true, true), e -> {});
        assert cow != null;
        ctx.assertValueEqual(
            CcaEntityTestMod.CAMEL_BASE_VITA, Vita.get(cow).getVitality(),
            Component.literal("Component data should transfer according to RespawnCopyStrategy -"));
        Cat cat = cow.convertTo(EntityType.CAT, ConversionParams.single(camel, true, true), e -> {});
        ctx.assertTrue("Component data should not transfer by default", Vita.get(cat).getVitality() < CcaEntityTestMod.NATURAL_VITA_CEILING);
        ctx.succeed();
    }
}
