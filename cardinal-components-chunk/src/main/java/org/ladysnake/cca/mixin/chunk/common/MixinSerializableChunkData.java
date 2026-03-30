/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2026 Ladysnake
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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.storage.TagValueInput;
import org.jspecify.annotations.Nullable;
import org.ladysnake.cca.internal.base.AbstractComponentContainer;
import org.ladysnake.cca.internal.base.ComponentsInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SerializableChunkData.class)
public abstract class MixinSerializableChunkData {
    @Unique
    private @Nullable CompoundTag cca$serializedComponents;

    @Inject(method = "parse", at = @At("RETURN"))
    private static void fromNbt(LevelHeightAccessor world, PalettedContainerFactory palettesFactory, CompoundTag nbt, CallbackInfoReturnable<SerializableChunkData> cir) {
        MixinSerializableChunkData ret = (MixinSerializableChunkData) (Object) cir.getReturnValue();
        if (ret != null) {
            ret.cca$serializedComponents = nbt.getCompound(AbstractComponentContainer.NBT_KEY).orElse(null);
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void convert(ServerLevel world, PoiManager poiStorage, RegionStorageInfo key, ChunkPos expectedPos, CallbackInfoReturnable<ProtoChunk> cir) {
        CompoundTag tag = cca$serializedComponents;
        if (tag == null) return;
        ProtoChunk ret = cir.getReturnValue();
        ChunkAccess chunk = ret instanceof ImposterProtoChunk ? ((ImposterProtoChunk) ret).getWrapped() : ret;
        try (var errorReporter = new ProblemReporter.ScopedCollector(ComponentsInternals.LOGGER)) {
            chunk.asComponentProvider().getComponentContainer().readOrphanData(TagValueInput.create(errorReporter, world.registryAccess(), tag));
        }
        // If components have been removed, we need to make the chunk save again
        if (tag.size() > 0) {
            chunk.markUnsaved();
        }
    }

    @Inject(method = "copyOf", at = @At("RETURN"))
    private static void fromChunk(ServerLevel world, ChunkAccess chunk, CallbackInfoReturnable<SerializableChunkData> cir) {
        MixinSerializableChunkData ret = (MixinSerializableChunkData) (Object) cir.getReturnValue();
        if (ret != null) {
            ret.cca$serializedComponents = chunk.asComponentProvider().getComponentContainer().toOrphanTag(world.registryAccess());
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void serialize(CallbackInfoReturnable<CompoundTag> cir) {
        if (cca$serializedComponents != null) {
            cir.getReturnValue().put(AbstractComponentContainer.NBT_KEY, cca$serializedComponents);
        }
    }
}
