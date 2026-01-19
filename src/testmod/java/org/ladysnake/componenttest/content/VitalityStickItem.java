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
package org.ladysnake.componenttest.content;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Team;
import org.ladysnake.cca.test.base.Vita;
import org.ladysnake.cca.test.block.CcaBlockTestMod;
import org.ladysnake.cca.test.world.AmbientVita;
import org.ladysnake.componenttest.content.vita.ItemVita;

import java.util.Optional;
import java.util.function.Consumer;

public class VitalityStickItem extends Item {
    public VitalityStickItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vita vita = ItemVita.maybeGet(stack).orElseThrow();
        if (!world.isClientSide()) {
            if (player.isShiftKeyDown()) {
                Vita src = vita.getVitality() > 0 ? vita : Vita.get(player);
                AmbientVita worldVita = (AmbientVita) Vita.get(
                        world.getRandom().nextInt(10) == 0
                                ? world.getLevelData()
                                : world
                );
                src.transferTo(worldVita, 1);
                worldVita.syncWithAll(((ServerLevel)world).getServer());
            } else if (vita.getVitality() > 0) {
                vita.transferTo(Vita.get(player), vita.getVitality());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // only on client side, to confirm that sync works
        if (context.getLevel().isClientSide() && context.getPlayer() != null) {
            Vita vita = CcaBlockTestMod.VITA_API_LOOKUP.find(
                context.getLevel(),
                context.getClickedPos(),
                context.getClickedFace()
            );
            if (vita != null) {
                context.getPlayer().displayClientMessage(Component.translatable("componenttest:action.block_vitality",
                    vita.getVitality()), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity holder) {
        // The entity may not have the component, but the stack always does.
        Vita.KEY.maybeGet(target)
                .ifPresent(src -> ItemVita.maybeGet(stack).ifPresent(dest -> src.transferTo(dest, 1)));

        Team team = holder.getTeam();
        if (team != null) {
            Optional<Vita> vita = Vita.KEY.maybeGet(target.getTeam());
            if (vita.isEmpty()) {
                vita = Vita.KEY.maybeGet(target);
            }
            vita.ifPresent(v -> v.transferTo(Vita.get(team), 1));
        }

        stack.hurtAndBreak(1, holder, EquipmentSlot.MAINHAND);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, displayComponent, tooltip, type);
        tooltip.accept(Component.translatable("componenttest:tooltip.vitality", ItemVita.getOrEmpty(stack).getVitality()));
        LocalPlayer holder = Minecraft.getInstance().player;
        if (holder != null) {
            tooltip.accept(Component.translatable("componenttest:tooltip.self_vitality", Vita.KEY.get(holder).getVitality()));
        }
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level world, BlockPos pos, LivingEntity user) {
        return !(user instanceof Player player && player.isCreative());
    }
}
