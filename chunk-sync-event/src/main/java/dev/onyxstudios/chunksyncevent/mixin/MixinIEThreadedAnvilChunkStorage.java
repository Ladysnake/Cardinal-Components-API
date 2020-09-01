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
package dev.onyxstudios.chunksyncevent.mixin;

import com.qouteall.immersive_portals.mixin.common.chunk_sync.MixinThreadedAnvilChunkStorage_C;
import dev.onyxstudios.chunksyncevent.InitialChunkSyncCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnusedMixin")    // added by the mixin plugin
@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinIEThreadedAnvilChunkStorage {
    // Big brain mcdev complains about mixin reference in mixin annotation
    @SuppressWarnings("ReferenceToMixin")
    @Dynamic(mixin = MixinThreadedAnvilChunkStorage_C.class)
    @Inject(method = "updateEntityTrackersAfterSendingChunkPacket", at = @At("HEAD"), remap = false)
    private void updateEntityTrackersAfterSendingChunkPacket(WorldChunk chunk, ServerPlayerEntity player, CallbackInfo ci) {
        InitialChunkSyncCallback.EVENT.invoker().onInitialChunkSync(player, chunk);
    }
}
