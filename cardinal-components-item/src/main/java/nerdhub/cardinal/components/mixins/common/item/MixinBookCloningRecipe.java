package nerdhub.cardinal.components.mixins.common.item;

import nerdhub.cardinal.components.internal.CardinalItemInternals;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BookCloningRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BookCloningRecipe.class)
public class MixinBookCloningRecipe {
    @Inject(
        method = "craft",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setTag(Lnet/minecraft/nbt/CompoundTag;)V"),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void craft(CraftingInventory craftingInventory, CallbackInfoReturnable<ItemStack> cir, int copies, ItemStack original, ItemStack copy) {
        CardinalItemInternals.copyComponents(original, copy);
    }
}
