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
package nerdhub.cardinal.components.api.util.sync;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.netty.buffer.Unpooled;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.component.extension.TypeAwareComponent;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Default implementations of {@link SyncedComponent} methods, specialized for world components
 */
public interface WorldSyncedComponent extends BaseSyncedComponent {
    /**
     * {@link CustomPayloadS2CPacket} channel for default world component synchronization.
     *
     * <p> Packets emitted on this channel must begin with the
     * {@link ComponentType#getId() component's type} (as an Identifier).
     *
     * <p> Components synchronized through this channel will have {@linkplain SyncedComponent#processPacket(PacketContext, PacketByteBuf)}
     * called on the game thread.
     */
    Identifier PACKET_ID = new Identifier("cardinal-components", "world_sync");

    World getWorld();

    /**
     * {@inheritDoc}
     * @implNote The default implementation should generally be overridden.
     * This implementation performs a linear-time lookup on the provider to find the component type
     * this component is associated with.
     * Implementing classes can nearly always provide a better implementation.
     */
    @Override
    default ComponentType<?> getComponentType() {
        return TypeAwareComponent.lookupComponentType(ComponentProvider.fromWorld(this.getWorld()), this);
    }

    @Override
    default void sync() {
        if (!this.getWorld().isClient) {
            PlayerStream.world(this.getWorld()).map(ServerPlayerEntity.class::cast).forEach(this::syncWith);
        }
    }

    @Override
    default void syncWith(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
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
