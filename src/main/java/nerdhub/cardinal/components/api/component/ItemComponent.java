package nerdhub.cardinal.components.api.component;

import net.minecraft.nbt.CompoundTag;

/**
 * base class for item components.<br/>
 * provides basic serialization capability
 */
public interface ItemComponent {

    void fromTag(CompoundTag tag);

    CompoundTag toTag(CompoundTag tag);

    /**
     * used for copying item components to another stack
     */
    ItemComponent newInstance();

    /**
     * used for comparing two instances of item components
     */
    boolean isEqual(ItemComponent other);
}
