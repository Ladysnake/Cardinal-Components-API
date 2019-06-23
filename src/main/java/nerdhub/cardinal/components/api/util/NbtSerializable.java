package nerdhub.cardinal.components.api.util;

import net.minecraft.nbt.CompoundTag;

public interface NbtSerializable {
    /**
     * Reads this object's properties from a {@link CompoundTag}.
     *
     * @param tag a {@code CompoundTag} on which this object's serializable data has been written
     * @implNote implementations must not assert that the data written on the tag corresponds to any
     * specific scheme, as saved data is susceptible to external tempering, and may come from an earlier
     * version.
     */
    void fromTag(CompoundTag tag);

    /**
     * Writes this object's properties to a {@link CompoundTag}.
     *
     * @param tag a {@code CompoundTag} on which to write this component's serializable data
     * @return {@code tag} for easy chaining
     */
    CompoundTag toTag(CompoundTag tag);
}
