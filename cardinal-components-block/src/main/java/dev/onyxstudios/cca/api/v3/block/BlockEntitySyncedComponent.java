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
package dev.onyxstudios.cca.api.v3.block;

import io.netty.buffer.Unpooled;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.util.sync.BaseSyncedComponent;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockEntitySyncedComponent extends BaseSyncedComponent {
    /**
     * {@link CustomPayloadS2CPacket} channel for default entity component synchronization.
     *
     * <p> Packets emitted on this channel must begin with, in order, the {@link BlockEntity#getType() BE type} (as an identifier),
     * the {@link BlockEntity#getPos() position} (as 3 consecutive XYZ int values),
     * and the {@link ComponentType#getId() component's type} (as an Identifier).
     *
     * <p> Components synchronized through this channel will have {@linkplain SyncedComponent#processPacket(PacketContext, PacketByteBuf)}
     * called on the game thread.
     */
    Identifier PACKET_ID = new Identifier("cardinal-components", "block_entity_sync");

    BlockEntity getBlockEntity();

    @Override
    default void sync() {
        BlockEntity holder = this.getBlockEntity();
        World world = holder.getWorld();
        if (world != null && !world.isClient) {
            PlayerStream.watching(holder).map(ServerPlayerEntity.class::cast).forEach(this::syncWith);
        }
    }

    @Override
    default void syncWith(ServerPlayerEntity player) {
        BlockEntity holder = this.getBlockEntity();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIdentifier(BlockEntityType.getId(holder.getType()));
        BlockPos pos = holder.getPos();
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeIdentifier(this.getComponentType().getId());
        this.writeToPacket(buf);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf);
    }

    /**
     * {@inheritDoc}
     *
     * @see #PACKET_ID
     */
    @Override
    default void processPacket(PacketContext ctx, PacketByteBuf buf) {
        assert ctx.getTaskQueue().isOnThread();
        this.readFromPacket(buf);
    }
}
