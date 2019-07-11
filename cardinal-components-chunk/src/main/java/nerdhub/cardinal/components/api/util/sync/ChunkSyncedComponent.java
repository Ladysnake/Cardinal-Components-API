/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
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
package nerdhub.cardinal.components.api.util.sync;

import io.netty.buffer.Unpooled;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.util.ChunkComponent;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Default implementations of {@link SyncedComponent} methods, specialized for chunk components
 */
public interface ChunkSyncedComponent extends ChunkComponent, BaseSyncedComponent {
    /**
     * {@link CustomPayloadS2CPacket} channel for default chunk component synchronization.
     * Packets emitted on this channel must begin with, in order, the chunk x position ({@code int}),
     * the chunk z position ({@code int}), and the {@link Identifier} for the component's type.
     */
    Identifier PACKET_ID = new Identifier("cardinal-components", "chunk_sync");

    Chunk getChunk();

    @Override
    default void markDirty() {
        if (this.getChunk() instanceof WorldChunk) {
            WorldChunk chunk = (WorldChunk) this.getChunk();
            if (!chunk.getWorld().isClient) {
                PlayerStream.watching(chunk.getWorld(), chunk.getPos()).map(ServerPlayerEntity.class::cast).forEach(this::syncWith);
            }
        }
    }

    @Override
    default void syncWith(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        ChunkPos pos = this.getChunk().getPos();
        buf.writeInt(pos.x);
        buf.writeInt(pos.z);
        buf.writeIdentifier(this.getComponentType().getId());
        this.writeToPacket(buf);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf);
    }

    @Override
    default void processPacket(PacketContext ctx, PacketByteBuf buf) {
        assert ctx.getTaskQueue().isOnThread();
        this.readFromPacket(buf);
    }
}
