package nerdhub.cardinal.components.mixins.common.item;

import nerdhub.cardinal.components.internal.CardinalItemInternals;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShulkerBoxColoringRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ShulkerBoxColoringRecipe.class)
public class MixinShulkerBoxColoringRecipe {
    @Inject(
        method = "craft",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasTag()Z"),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void craft(CraftingInventory craftingInventory, CallbackInfoReturnable<ItemStack> cir, ItemStack original, DyeItem dye, ItemStack copy) {
        CardinalItemInternals.copyComponents(original, copy);
    }
}
