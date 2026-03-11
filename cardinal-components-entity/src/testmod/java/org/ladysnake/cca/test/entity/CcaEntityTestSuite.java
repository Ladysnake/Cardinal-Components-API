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
package org.ladysnake.cca.test.entity;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.ladysnake.cca.test.base.LoadAwareTestComponent;
import org.ladysnake.cca.test.base.Vita;

public class CcaEntityTestSuite {
    @GameTest
    public void bucketableWorks(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        player.interactionManager.changeGameMode(GameMode.SURVIVAL);
        player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        BlockPos pos = new BlockPos(2, 0, 2);
        var axolotl = ctx.spawnMob(EntityType.AXOLOTL, pos);
        axolotl.getComponent(Vita.KEY).setVitality(3);
        Bucketable.tryBucket(player, Hand.MAIN_HAND, axolotl);
        ((EntityBucketItem) Items.AXOLOTL_BUCKET).onEmptied(player, ctx.getWorld(), player.getStackInHand(Hand.MAIN_HAND), ctx.getAbsolutePos(pos));
        ctx.expectEntityWithDataEnd(pos, EntityType.AXOLOTL, a -> a.getComponent(Vita.KEY).getVitality(), 3);
    }

    @GameTest
    public void loadEventsWork(TestContext ctx) {
        ShulkerEntity shulker = new ShulkerEntity(EntityType.SHULKER, ctx.getWorld());
        Vec3d vec3d = ctx.getAbsolute(new Vec3d(1, 0, 1));
        shulker.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, shulker.getYaw(), shulker.getPitch());
        ctx.assertEquals(
            0, LoadAwareTestComponent.KEY.get(shulker).getLoadCounter(),
            Text.literal("Load counter should not be incremented until the entity joins the world -")
        );
        ctx.getWorld().spawnEntity(shulker);
        ctx.assertEquals(
            1, LoadAwareTestComponent.KEY.get(shulker).getLoadCounter(),
            Text.literal("Load counter should be incremented once when the entity joins the world -")
        );
        shulker.remove(Entity.RemovalReason.DISCARDED);
        ctx.waitAndRun(1, () -> {
            ctx.assertEquals(
                0,
                LoadAwareTestComponent.KEY.get(shulker).getLoadCounter(),
                Text.literal("Load counter should be decremented when the entity leaves the world -")
            );
            ctx.complete();
        });
    }

    @GameTest
    public void moddedEntitiesWork(TestContext ctx) {
        ctx.spawnEntity(CcaEntityTestMod.TEST_ENTITY, 0, 0, 0);
        ctx.complete();
    }

    @GameTest
    public void respawnHappensOnConversion(TestContext ctx) {
        CamelEntity camel = ctx.spawnEntity(EntityType.CAMEL, 0, 0, 0);
        CowEntity cow = camel.convertTo(EntityType.COW, EntityConversionContext.create(camel, true, true), e -> {});
        assert cow != null;
        ctx.assertEquals(
            CcaEntityTestMod.CAMEL_BASE_VITA, Vita.get(cow).getVitality(),
            Text.literal("Component data should transfer according to RespawnCopyStrategy -"));
        CatEntity cat = cow.convertTo(EntityType.CAT, EntityConversionContext.create(camel, true, true), e -> {});
        ctx.assertTrue("Component data should not transfer by default", Vita.get(cat).getVitality() < CcaEntityTestMod.NATURAL_VITA_CEILING);
        ctx.complete();
    }
}
