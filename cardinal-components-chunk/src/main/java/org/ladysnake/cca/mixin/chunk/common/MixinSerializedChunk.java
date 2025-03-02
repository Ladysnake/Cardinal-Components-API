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
package org.ladysnake.cca.mixin.chunk.common;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.SerializedChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.internal.base.AbstractComponentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SerializedChunk.class)
public abstract class MixinSerializedChunk {
    @Unique
    private @Nullable NbtCompound cca$serializedComponents;

    @Inject(method = "fromNbt", at = @At("RETURN"))
    private static void fromNbt(HeightLimitView world, DynamicRegistryManager registryManager, NbtCompound nbt, CallbackInfoReturnable<SerializedChunk> cir) {
        MixinSerializedChunk ret = (MixinSerializedChunk) (Object) cir.getReturnValue();
        if (ret != null) {
            ret.cca$serializedComponents = nbt.getCompound(AbstractComponentContainer.NBT_KEY).orElse(null);
        }
    }

    @Inject(method = "convert", at = @At("RETURN"))
    private void convert(ServerWorld world, PointOfInterestStorage poiStorage, StorageKey key, ChunkPos expectedPos, CallbackInfoReturnable<ProtoChunk> cir) {
        NbtCompound tag = cca$serializedComponents;
        if (tag == null) return;
        ProtoChunk ret = cir.getReturnValue();
        Chunk chunk = ret instanceof WrapperProtoChunk ? ((WrapperProtoChunk) ret).getWrappedChunk() : ret;
        chunk.asComponentProvider().getComponentContainer().fromOrphanTag(tag, world.getRegistryManager());
        // If components have been removed, we need to make the chunk save again
        if (tag.getSize() > 0) {
            chunk.markNeedsSaving();
        }
    }

    @Inject(method = "fromChunk", at = @At("RETURN"))
    private static void fromChunk(ServerWorld world, Chunk chunk, CallbackInfoReturnable<SerializedChunk> cir) {
        MixinSerializedChunk ret = (MixinSerializedChunk) (Object) cir.getReturnValue();
        if (ret != null) {
            ret.cca$serializedComponents = chunk.asComponentProvider().getComponentContainer().toOrphanTag(world.getRegistryManager());
        }
    }

    @Inject(method = "serialize", at = @At("RETURN"))
    private void serialize(CallbackInfoReturnable<NbtCompound> cir) {
        if (cca$serializedComponents != null) {
            cir.getReturnValue().put(AbstractComponentContainer.NBT_KEY, cca$serializedComponents);
        }
    }
}
