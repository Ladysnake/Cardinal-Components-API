package nerdhub.cardinal.components.mixins.common.item;

import nerdhub.cardinal.components.internal.CardinalItemInternals;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SmithingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SmithingScreenHandler.class)
public abstract class MixinSmithingScreenHandler {
    @Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;copy()Lnet/minecraft/nbt/CompoundTag;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void updateResult(CallbackInfo ci, ItemStack input1, ItemStack input2, Item it, ItemStack result) {
        CardinalItemInternals.copyComponents(input1, result);
    }
}
