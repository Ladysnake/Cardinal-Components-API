package nerdhub.cardinal.componentstest;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
        if (vita.getVitality() > 0) {
            vita.transferTo(CardinalComponentsTest.VITA.get(player), vita.getVitality());
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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Component> lines, TooltipContext ctx) {
        super.appendTooltip(stack, world, lines, ctx);
        lines.add(new TranslatableComponent("componenttest:tooltip.vitality", CardinalComponentsTest.VITA.get(stack).getVitality()));
    }

    @Override
    public void initComponents(ItemStack stack, ComponentContainer components) {
        components.put(CardinalComponentsTest.VITA, new BaseVita());
    }
}
