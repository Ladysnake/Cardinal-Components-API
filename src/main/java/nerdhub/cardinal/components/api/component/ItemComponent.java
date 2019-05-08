package nerdhub.cardinal.components.api.component;

import net.minecraft.nbt.CompoundTag;

public interface ItemComponent {

    void fromTag(CompoundTag tag);

    void toTag(CompoundTag tag);
}
