/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.cca.mixin.item.common;

import net.minecraft.block.cauldron.CauldronBehavior;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CauldronBehavior.class)
public interface MixinCauldronBehavior {
/*  FIXME mixins still chokes up on interface injects
    @SuppressWarnings({"UnresolvedMixinReference", "PublicStaticMixinMember"})   // lambda injection
    @ModifyVariable(
        method = "method_32215",    // CLEAN_SHULKER_BOX#interact
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasTag()Z", ordinal = 0),
        ordinal = 1
    )
    static ItemStack addStack(ItemStack cleanShulker, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        CardinalItemInternals.copyComponents(player.getStackInHand(hand), cleanShulker);
        return cleanShulker;
    }
*/
}
