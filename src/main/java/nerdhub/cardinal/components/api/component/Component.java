package nerdhub.cardinal.components.api.component;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * base class for components.<br/>
 * provides basic serialization capability.<br/>
 * <p>Item Components <strong>MUST</strong> override and implement all methods of this interface!</p>
 */
public interface Component {

    default void deserialize(CompoundTag tag) {
        throw new IllegalStateException("Tried to deserialize on Block, or method not implemented!");
    }

    default CompoundTag serialize(CompoundTag tag) {
        throw new IllegalStateException("Tried to serialize on Block, or not method not implemented!");
    }

    /**
     * used for copying components to another {@link ItemStack} or {@link Entity}
     */
    default Component newInstance() {
        throw new IllegalStateException("Tried to serialize on Block, or not implemented on Item!");
    }

    /**
     * used for comparing two instances of item components
     */
    default boolean isComponentEqual(Component other) {
        throw new IllegalStateException("Tried to compare block component, or not implemented on Item!");
    }
}
