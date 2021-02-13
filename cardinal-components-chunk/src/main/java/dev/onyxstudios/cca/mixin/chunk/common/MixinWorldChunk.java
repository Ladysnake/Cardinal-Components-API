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

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.sync.ComponentPacketWriter;
import dev.onyxstudios.cca.internal.chunk.ComponentsChunkNetworking;
import dev.onyxstudios.cca.internal.chunk.StaticChunkComponentPlugin;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk implements Chunk, ComponentProvider {
    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract ChunkPos getPos();

    @Unique
    private ComponentContainer components;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/biome/source/BiomeArray;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/TickScheduler;Lnet/minecraft/world/TickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Ljava/util/function/Consumer;)V", at = @At("RETURN"))
    private void initComponents(CallbackInfo ci) {
        this.components = StaticChunkComponentPlugin.createContainer(this);
    }

    @Nonnull
    @Override
    public ComponentContainer getComponentContainer() {
        return this.components;
    }

    @Override
    public Iterator<ServerPlayerEntity> getRecipientsForComponentSync() {
        if (!this.getWorld().isClient()) {
            return PlayerStream.watching(this.getWorld(), this.getPos()).map(ServerPlayerEntity.class::cast).iterator();
        }
        return Collections.emptyIterator();
    }

    @Override
    public <C extends AutoSyncedComponent> CustomPayloadS2CPacket toComponentPacket(PacketByteBuf buf, ComponentKey<? super C> key, ComponentPacketWriter writer, ServerPlayerEntity recipient) {
        ChunkPos pos = this.getPos();
        buf.writeInt(pos.x);
        buf.writeInt(pos.z);
        buf.writeIdentifier(key.getId());
        writer.writeSyncPacket(buf, recipient);
        return new CustomPayloadS2CPacket(ComponentsChunkNetworking.PACKET_ID, buf);
    }

    @Inject(method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Ljava/util/function/Consumer;)V", at = @At("RETURN"))
    private void copyFromProto(ServerWorld world, ProtoChunk proto, Consumer<WorldChunk> consumer, CallbackInfo ci) {
        this.components.copyFrom(ComponentProvider.fromChunk(proto).getComponentContainer());
    }
}
