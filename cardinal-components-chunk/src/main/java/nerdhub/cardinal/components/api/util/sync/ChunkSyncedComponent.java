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
import dev.onyxstudios.cca.internal.chunk.ComponentsChunkNetworking;
import io.netty.buffer.Unpooled;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.component.extension.TypeAwareComponent;
import nerdhub.cardinal.components.api.util.ChunkComponent;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.ApiStatus;

/**
 * Default implementations of {@link SyncedComponent} methods, specialized for chunk components
 * @deprecated use {@link dev.onyxstudios.cca.api.v3.component.AutoSyncedComponent}
 * @see <a href=https://github.com/OnyxStudios/Cardinal-Components-API/wiki/Synchronizing-components>information on the V3 API</a>
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
public interface ChunkSyncedComponent<C extends Component> extends ChunkComponent<C>, BaseSyncedComponent {
    /**
     * {@link CustomPayloadS2CPacket} channel for default chunk component synchronization.
     *
     * <p> Packets emitted on this channel must begin with, in order, the chunk x position ({@code int}),
     * the chunk z position ({@code int}), and the {@link Identifier} for the component's type.
     *
     * <p> Components synchronized through this channel will have {@linkplain SyncedComponent#processPacket(PacketContext, PacketByteBuf)}
     * called on the game thread.
     */
    Identifier PACKET_ID = ComponentsChunkNetworking.PACKET_ID;

    Chunk getChunk();

    /**
     * {@inheritDoc}
     * @implNote The default implementation should generally be overridden.
     * This implementation performs a linear-time lookup on the provider to find the component type
     * this component is associated with.
     * Implementing classes can nearly always provide a better implementation.
     */
    @Override
    default ComponentType<? super C> getComponentType() {
        return TypeAwareComponent.lookupComponentType(ComponentProvider.fromChunk(this.getChunk()), this);
    }

    @Override
    default void sync() {
        ComponentProvider.fromChunk(this.getChunk()).getRecipientsForComponentSync().forEachRemaining(this::syncWith);
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
