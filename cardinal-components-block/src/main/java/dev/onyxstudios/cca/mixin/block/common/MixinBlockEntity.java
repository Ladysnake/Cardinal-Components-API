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
package dev.onyxstudios.cca.mixin.block.common;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.internal.block.BlockEntityComponentProvider;
import dev.onyxstudios.cca.internal.block.CardinalBlockInternals;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements BlockEntityComponentProvider {
    @Unique
    private ComponentContainer<?> components;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(BlockEntityType<?> type, CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        this.components = CardinalBlockInternals.createComponents(self);
    }

    @Override
    public final ComponentContainer<?> getComponentContainer() {
        return this.components;
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void loadComponents(BlockState state, CompoundTag tag, CallbackInfo ci) {
        CompoundTag components = tag.getCompound("cardinal_components");

        if (components.contains("core" ,NbtType.COMPOUND)) {
            this.components.fromTag(components.getCompound("core"));
        }

        for (Direction side : Direction.values()) {
            if (components.contains(side.asString(), NbtType.COMPOUND)) {
                this.getComponentContainer().fromTag(components.getCompound(side.asString()));
            }
        }
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void saveComponents(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag components = new CompoundTag();
        CompoundTag coreTag = this.components.toTag(new CompoundTag());

        if (!coreTag.isEmpty()) {
            components.put("core", coreTag);
        }

        for (Direction side : Direction.values()) {
            CompoundTag sideTag = this.getComponentContainer().toTag(new CompoundTag());
            if (!sideTag.isEmpty()) {
                components.put(side.asString(), sideTag);
            }
        }
        tag.put("cardinal_components", components);
    }
}
