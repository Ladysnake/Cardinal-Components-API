package nerdhub.cardinal.components.api.util.component.sync;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public interface EntitySyncedComponent extends SyncedComponent {
    Identifier PACKET_ID = new Identifier("cardinal_components", "entity_sync");

    Entity getEntity();

    @Override
    default void markDirty() {
        PlayerStream.watching(this.getEntity()).map(ServerPlayerEntity.class::cast).forEach(this::syncWith);
    }

    @Override
    default void syncWith(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        this.writeToPacket(buf);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf);
    }

    @Override
    default void writeToPacket(PacketByteBuf buf) {
        buf.writeCompoundTag(this.toTag(new CompoundTag()));
    }

    @Override
    default void readFromPacket(PacketByteBuf buf) {
        CompoundTag tag = buf.readCompoundTag();
        if (tag != null) {
            this.fromTag(tag);
        }
    }
}
