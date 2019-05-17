package nerdhub.cardinal.components.api.component;

import net.minecraft.nbt.CompoundTag;

/**
 * base class for components.<br/>
 * provides basic serialization capability.<br/>
 * <p>Item Components <strong>MUST</strong> override and implement all methods of this interface!</p>
 */
public interface Component {

    default void fromItemTag(CompoundTag tag) {
        throw new IllegalStateException("Tried to deserialize on Block, or not implemented on Item!");
    }

    default CompoundTag toItemTag(CompoundTag tag) {
        throw new IllegalStateException("Tried to serialize on Block, or not implemented on Item!");
    }

    /**
     * used for copying item components to another stack
     */
    default Component newInstanceForItemStack() {
        throw new IllegalStateException("Tried to serialize on Block, or not implemented on Item!");
    }

    /**
     * used for comparing two instances of item components
     */
    default boolean isComponentEqual(Component other) {
        throw new IllegalStateException("Tried to compare block component, or not implemented on Item!");
    }
}
