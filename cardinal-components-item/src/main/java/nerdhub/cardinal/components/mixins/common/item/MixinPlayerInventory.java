package nerdhub.cardinal.components.mixins.common.item;

import nerdhub.cardinal.components.internal.CardinalItemInternals;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerInventory.class)
public abstract class MixinPlayerInventory {
    @ModifyVariable(
        method = "addStack(ILnet/minecraft/item/ItemStack;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasTag()Z"),
        ordinal = 1
    )
    private ItemStack addStack(ItemStack copy, int slot, ItemStack stack) {
        CardinalItemInternals.copyComponents(stack, copy);
        return copy;
    }
}
