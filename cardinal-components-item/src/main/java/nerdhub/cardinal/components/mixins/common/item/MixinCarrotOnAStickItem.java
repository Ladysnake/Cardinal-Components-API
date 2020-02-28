package nerdhub.cardinal.components.mixins.common.item;

import nerdhub.cardinal.components.internal.CardinalItemInternals;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CarrotOnAStickItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CarrotOnAStickItem.class)
public class MixinCarrotOnAStickItem {
    @ModifyVariable(
        method = "use",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setTag(Lnet/minecraft/nbt/CompoundTag;)V"),
        ordinal = 1
    )
    private ItemStack addStack(ItemStack cleanShulker, World world, PlayerEntity user, Hand hand) {
        CardinalItemInternals.copyComponents(user.getStackInHand(hand), cleanShulker);
        return cleanShulker;
    }

}
