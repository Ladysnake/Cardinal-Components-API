package nerdhub.cardinal.components.mixins.common.item;

import nerdhub.cardinal.components.api.util.Components;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity {
    @Shadow
    public abstract ItemStack getStack();

    @Inject(
        method = "tryMerge(Lnet/minecraft/entity/ItemEntity;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;merge(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;)V"),
        cancellable = true
    )
    private void stopDifferentComponentsMerging(ItemEntity other, CallbackInfo ci) {
        if (!Components.areComponentsEqual(this.getStack(), other.getStack())) {
            ci.cancel();
        }
    }
}
