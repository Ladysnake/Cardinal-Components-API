/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 OnyxStudios
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
package nerdhub.cardinal.components.mixins.common.item;

import nerdhub.cardinal.components.internal.ItemStackAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(PacketByteBuf.class)
public abstract class MixinPacketByteBuf {

    @Shadow public abstract PacketByteBuf writeCompoundTag(@Nullable CompoundTag compoundTag_1);

    @Shadow @Nullable public abstract CompoundTag readCompoundTag();

    @SuppressWarnings("UnresolvedMixinReference")    // Optifine-specific injection
    @Inject(
        method = {
            "writeItemStack(Lnet/minecraft/class_1799;Z)Lnet/minecraft/class_2540;" // writeItemStack(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/util/PacketByteBuf;
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/PacketByteBuf;writeCompoundTag(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/util/PacketByteBuf;",
            shift = At.Shift.AFTER
        )
    )
    private void writeItemStack(ItemStack stack, boolean limitedTag, CallbackInfoReturnable<PacketByteBuf> cir) {
        this.cardinal_writeItemStack(stack);
    }

    @Inject(
        method = {
            "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/PacketByteBuf;"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/PacketByteBuf;writeCompoundTag(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/util/PacketByteBuf;",
            shift = At.Shift.AFTER
        )
    )
    @Surrogate
    private void writeItemStack(ItemStack stack, CallbackInfoReturnable<PacketByteBuf> cir) {
        this.cardinal_writeItemStack(stack);
    }

    @Unique
    private void cardinal_writeItemStack(ItemStack stack) {
        //noinspection ConstantConditions
        this.writeCompoundTag(((ItemStackAccessor)(Object)stack).cardinal_getComponentContainer().toTag(new CompoundTag()));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Inject(method = "readItemStack", at = @At(value = "RETURN"))
    private void readStack(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if(!stack.isEmpty()) {
            ((ItemStackAccessor) ((Object) stack)).cardinal_getComponentContainer().fromTag(this.readCompoundTag());
        }
    }
}
