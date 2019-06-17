package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentType;
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
public interface SidedComponentContainer {
    ComponentContainer get(@Nullable Direction side);

    /**
     * Convenience method to attach a component to a sided container.
     * If {@code side} is {@code null}, the component will also be attached
     * to every side that does not already have a component of this type.
     */
    default <T extends Component> void put(@Nullable Direction side, ComponentType<T> type, T component) {
        if (side == null) {
            for (Direction dir : Direction.values()) {
                ComponentContainer cc = get(dir);
                if (!cc.containsKey(type)) {
                    cc.put(type, component);
                }
            }
        }
        get(side).put(type, component);
    }

    void fromTag(CompoundTag serialized);

    CompoundTag toTag(CompoundTag tag);
}