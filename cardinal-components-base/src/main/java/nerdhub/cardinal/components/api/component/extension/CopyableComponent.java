package nerdhub.cardinal.components.api.component.extension;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;

/**
 * A component that can copy its data from another component of the same type.
 *
 * @param <C> this component's type
 * @since 2.3.0
 */
public interface CopyableComponent<C extends Component> extends TypeAwareComponent {
    default void copyFrom(C other) {
        this.fromTag(other.toTag(new CompoundTag()));
    }

    @Override
    ComponentType<C> getComponentType();
}
