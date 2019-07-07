package nerdhub.cardinal.components.api.util.sync;

import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.component.extension.TypeAwareComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public interface BaseSyncedComponent extends SyncedComponent, TypeAwareComponent {

    default void writeToPacket(PacketByteBuf buf) {
        buf.writeCompoundTag(this.toTag(new CompoundTag()));
    }

    default void readFromPacket(PacketByteBuf buf) {
        CompoundTag tag = buf.readCompoundTag();
        if (tag != null) {
            this.fromTag(tag);
        }
    }
}
