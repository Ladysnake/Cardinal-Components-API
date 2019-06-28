package nerdhub.cardinal.components.api.component;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.io.Serializable;

/**
 * base class for components.<br/>
 * provides basic serialization capability.<br/>
 * <p>Item Components <strong>MUST</strong> override and implement all methods of this interface!</p>
 */
public interface Component extends Serializable {

    /**
     * Reads this component's properties from a {@link CompoundTag}.
     *
     * @param tag a {@code CompoundTag} on which this component's serializable data has been written
     * @throws UnsupportedOperationException if this {@code Component} does not support serialization to NBT
     * @implSpec implementations must not assert that the data written on the tag corresponds to any
     * specific scheme, as saved data is susceptible to external tempering, and may come from an earlier
     * version.
     */
    void fromTag(CompoundTag tag);

    /**
     * Writes this component's properties to a {@link CompoundTag}.
     *
     * @param tag a {@code CompoundTag} on which to write this component's serializable data
     * @return {@code tag}
     * @implSpec this method must <strong>NOT</strong> write any value associated with the {@code "componentId"} key
     * in the given tag.
     */
    CompoundTag toTag(CompoundTag tag);

    /**
     * used for copying components to another {@link ItemStack} or {@link Entity}
     */
    Component newInstance();

    /**
     * Used to compare two instances of components for equality.
     */
    default boolean isComponentEqual(Component other) {
        return this.equals(other);
    }
}
