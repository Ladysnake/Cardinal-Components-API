package nerdhub.cardinal.components.api.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A side-aware compound component container.
 * Each direction of a cubic space is associated with a separate component container.
 * <p> In this context, the {@code null} side acts as a default value.
 * Attaching a component to the {@code null} side generally
 * implies that it should be accessible from any side. A query for a component on
 * a {@code null} side should only be made when the caller does not have specific
 * side information, and will return only elements attached to the {@code null} side.
 */
public interface SidedContainerCompound {
    ComponentContainer get(@Nullable Direction side);

    void fromTag(CompoundTag serialized);

    CompoundTag toTag(CompoundTag tag);

    // TODO add a toMap() method
}