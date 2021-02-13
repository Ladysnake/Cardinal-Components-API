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
package dev.onyxstudios.cca.mixin.chunk.common;

import com.mojang.datafixers.DataFixer;
import dev.onyxstudios.cca.api.v3.chunk.ChunkSyncCallback;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage extends VersionedChunkStorage {
    public MixinThreadedAnvilChunkStorage(File file, DataFixer dataFixer, boolean bl) {
        super(file, dataFixer, bl);
    }

    @Inject(method = "sendChunkDataPackets", at = @At("RETURN"))
    private void sendChunkComponentsPackets(ServerPlayerEntity player, Packet<?>[] packets, WorldChunk chunk, CallbackInfo ci) {
        ChunkSyncCallback.EVENT.invoker().onChunkSync(player, chunk);
    }
}
