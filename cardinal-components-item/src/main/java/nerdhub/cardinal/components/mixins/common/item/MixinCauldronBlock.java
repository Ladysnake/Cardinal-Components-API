package nerdhub.cardinal.components.mixins.common.item;

import nerdhub.cardinal.components.internal.CardinalItemInternals;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CauldronBlock.class)
public class MixinCauldronBlock {
    @ModifyVariable(
        method = "onUse",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasTag()Z", ordinal = 0),
        ordinal = 1
    )
    private ItemStack addStack(ItemStack cleanShulker, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        CardinalItemInternals.copyComponents(player.getStackInHand(hand), cleanShulker);
        return cleanShulker;
    }

}
