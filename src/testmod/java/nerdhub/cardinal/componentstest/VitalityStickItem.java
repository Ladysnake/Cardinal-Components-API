/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
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
package nerdhub.cardinal.componentstest;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.extension.CloneableComponent;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import nerdhub.cardinal.componentstest.vita.BaseVita;
import nerdhub.cardinal.componentstest.vita.Vita;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class VitalityStickItem extends Item implements ItemComponentCallback {
    public VitalityStickItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Vita vita = CardinalComponentsTest.VITA.get(stack);
        if (vita.getVitality() > 0 && !world.isClient) {
            if (player.isSneaking()) {
                vita.transferTo(CardinalComponentsTest.VITA.get(world), 1);
            } else {
                vita.transferTo(CardinalComponentsTest.VITA.get(player), vita.getVitality());
            }
        }
        return new TypedActionResult<>(ActionResult.PASS, stack);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity holder) {
        // The entity may not have the component, but the stack always does.
        CardinalComponentsTest.VITA.maybeGet(target)
                .ifPresent(v -> v.transferTo(CardinalComponentsTest.VITA.get(stack), 1));
        return true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> lines, TooltipContext ctx) {
        super.appendTooltip(stack, world, lines, ctx);
        lines.add(new TranslatableText("componenttest:tooltip.vitality", CardinalComponentsTest.VITA.get(stack).getVitality()));
        PlayerEntity holder = MinecraftClient.getInstance().player;
        if (holder != null) {
            lines.add(new TranslatableText("componenttest:tooltip.self_vitality", CardinalComponentsTest.VITA.get(holder).getVitality()));
        }
    }

    @Override
    public void initComponents(ItemStack stack, ComponentContainer<CloneableComponent<?>> components) {
        components.put(CardinalComponentsTest.VITA, new BaseVita());
    }
}
