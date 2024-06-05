/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 Ladysnake
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
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import org.ladysnake.cca.internal.base.AbstractComponentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public abstract class MixinChunkSerializer {
    @Inject(method = "deserialize", at = @At("RETURN"))
    private static void deserialize(ServerWorld world, PointOfInterestStorage poiStorage, StorageKey key, ChunkPos chunkPos, NbtCompound tag, CallbackInfoReturnable<ProtoChunk> cir) {
        ProtoChunk ret = cir.getReturnValue();
        Chunk chunk = ret instanceof WrapperProtoChunk ? ((WrapperProtoChunk) ret).getWrappedChunk() : ret;
        chunk.asComponentProvider().getComponentContainer().fromTag(tag, world.getRegistryManager());
        // If components have been removed, we need to make the chunk save again
        if (tag.contains(AbstractComponentContainer.NBT_KEY, NbtElement.COMPOUND_TYPE)) {
            int remainingComponentCount = tag.getCompound(AbstractComponentContainer.NBT_KEY).getSize();
            chunk.setNeedsSaving(remainingComponentCount > 0);
        }
    }

    @Inject(method = "serialize", at = @At("RETURN"))
    private static void serialize(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound ret = cir.getReturnValue();
        chunk.asComponentProvider().getComponentContainer().toTag(ret, world.getRegistryManager());
    }
}
