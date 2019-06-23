package nerdhub.cardinal.components.api.util.component;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;

/**
 * A component that has no inherent properties and is only used to indicate the existence
 * of a property on an object.
 */
public interface MarkerComponent extends Component {
    MarkerComponent INSTANCE = new Impl();

    /**
     * Marker components are all equal to each other.
     */
    @Override
    boolean isComponentEqual(Component other);

    final class Impl implements MarkerComponent {

        @Override
        public void fromTag(CompoundTag tag) {
            // NO-OP
        }

        @Override
        public CompoundTag toTag(CompoundTag tag) {
            return tag;
        }

        @Override
        public MarkerComponent newInstance() {
            return INSTANCE;
        }

        @Override
        public boolean isComponentEqual(Component other) {
            return other instanceof MarkerComponent;
        }
    }
}
