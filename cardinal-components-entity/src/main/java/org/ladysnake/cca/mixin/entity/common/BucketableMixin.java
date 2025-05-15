/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2025 Ladysnake
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
package org.ladysnake.cca.mixin.entity.common;

import net.minecraft.entity.Bucketable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtReadView;
import net.minecraft.util.ErrorReporter;
import org.ladysnake.cca.internal.base.AbstractComponentContainer;
import org.ladysnake.cca.internal.base.ComponentsInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Bucketable.class)
public interface BucketableMixin {
    @Inject(method = "method_57302", at = @At("RETURN"))
    private static void writeComponentsToStack(MobEntity mobEntity, NbtCompound nbtCompound, CallbackInfo ci) {
        NbtCompound nbt = mobEntity.asComponentProvider().getComponentContainer().toOrphanTag(mobEntity.getRegistryManager());
        if (nbt != null) {
            nbtCompound.put(AbstractComponentContainer.NBT_KEY, nbt);
        }
    }

    @Inject(method = "copyDataFromNbt(Lnet/minecraft/entity/mob/MobEntity;Lnet/minecraft/nbt/NbtCompound;)V", at = @At("RETURN"))
    private static void readComponentsFromStack(MobEntity entity, NbtCompound nbt, CallbackInfo ci) {
        Optional<NbtCompound> componentsNbt = nbt.getCompound(AbstractComponentContainer.NBT_KEY);
        if (componentsNbt.isPresent()) {
            try (var errorReporter = new ErrorReporter.Logging(ComponentsInternals.LOGGER)) {
                entity.asComponentProvider().getComponentContainer().readOrphanData(NbtReadView.create(errorReporter, entity.getRegistryManager(), componentsNbt.get()));
            }
        }
    }
}
