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

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.chunk.CardinalComponentsChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk extends ChunkAccess implements ComponentProvider {
    public MixinLevelChunk(ChunkPos pos, UpgradeData upgradeData, LevelHeightAccessor heightLimitView, PalettedContainerFactory palettesFactory, long inhabitedTime, @Nullable LevelChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, palettesFactory, inhabitedTime, sectionArray, blendingData);
    }

    @Shadow
    public abstract Level getLevel();

    @Override
    public Iterable<ServerPlayer> getRecipientsForComponentSync() {
        if (!this.getLevel().isClientSide()) {
            return PlayerLookup.tracking((ServerLevel) this.getLevel(), this.getPos());
        }
        return List.of();
    }

    @Override
    public @Nullable <C extends AutoSyncedComponent> ComponentUpdatePayload<?> toComponentPacket(ComponentKey<? super C> key, boolean required, RegistryFriendlyByteBuf data) {
        return new ComponentUpdatePayload<>(
            CardinalComponentsChunk.PACKET_ID,
            this.getPos(),
            required,
            key.getId(),
            data
        );
    }

    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V", at = @At("RETURN"))
    private void copyFromProto(ServerLevel world, ProtoChunk proto, LevelChunk.PostLoadProcessor entityLoader, CallbackInfo ci) {
        this.getComponentContainer().copyFrom(proto.asComponentProvider().getComponentContainer(), world.registryAccess());
    }
}
