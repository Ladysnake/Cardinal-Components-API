/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.componenttest;

import dev.onyxstudios.componenttest.vita.AmbientVita;
import dev.onyxstudios.componenttest.vita.Vita;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class VitalityStickItem extends Item {
    public VitalityStickItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Vita vita = Vita.get(stack);
        if (!world.isClient) {
            if (player.isSneaking()) {
                Vita src = vita.getVitality() > 0 ? vita : Vita.get(player);
                AmbientVita worldVita = (AmbientVita) Vita.get(
                        world.random.nextInt(10) == 0
                                ? world.getLevelProperties()
                                : world
                );
                src.transferTo(worldVita, 1);
                worldVita.syncWithAll(((ServerWorld)world).getServer());
            } else if (vita.getVitality() > 0) {
                vita.transferTo(Vita.get(player), vita.getVitality());
            }
        }
        return TypedActionResult.pass(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // only on client side, to confirm that sync works
        if (context.getWorld().isClient && context.getPlayer() != null) {
            Vita vita = CardinalComponentsTest.VITA_API_LOOKUP.find(
                context.getWorld(),
                context.getBlockPos(),
                context.getSide()
            );
            if (vita != null) {
                context.getPlayer().sendMessage(new TranslatableText("componenttest:action.block_vitality",
                    vita.getVitality()), true);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity holder) {
        // The entity may not have the component, but the stack always does.
        TestComponents.VITA.maybeGet(target)
                .ifPresent(v -> v.transferTo(Vita.get(stack), 1));

        AbstractTeam team = holder.getScoreboardTeam();
        if (team != null) {
            Optional<Vita> vita = TestComponents.VITA.maybeGet(target.getScoreboardTeam());
            if (vita.isEmpty()) {
                vita = TestComponents.VITA.maybeGet(target);
            }
            vita.ifPresent(v -> v.transferTo(Vita.get(team), 1));
        }

        stack.damage(1, holder, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        return true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> lines, TooltipContext ctx) {
        super.appendTooltip(stack, world, lines, ctx);
        lines.add(new TranslatableText("componenttest:tooltip.vitality", TestComponents.VITA.get(stack).getVitality()));
        PlayerEntity holder = MinecraftClient.getInstance().player;
        if (holder != null) {
            lines.add(new TranslatableText("componenttest:tooltip.self_vitality", TestComponents.VITA.get(holder).getVitality()));
        }
    }

    @Override
    public boolean canMine(BlockState block, World world, BlockPos pos, PlayerEntity player) {
        return !player.isCreative();
    }
}
