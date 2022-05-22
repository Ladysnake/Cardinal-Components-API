/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2022 OnyxStudios
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
package dev.onyxstudios.cca.mixin.entity.common;

import net.minecraft.entity.Bucketable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bucketable.class)
public interface BucketableMixin {
    @Inject(method = "copyDataToStack(Lnet/minecraft/entity/mob/MobEntity;Lnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    private static void writeComponentsToStack(MobEntity entity, ItemStack stack, CallbackInfo ci) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            entity.asComponentProvider().getComponentContainer().toTag(nbt);
        }
    }

    @Inject(method = "copyDataFromNbt(Lnet/minecraft/entity/mob/MobEntity;Lnet/minecraft/nbt/NbtCompound;)V", at = @At("RETURN"))
    private static void readComponentsFromStack(MobEntity entity, NbtCompound nbt, CallbackInfo ci) {
        entity.asComponentProvider().getComponentContainer().fromTag(nbt);
    }
}
