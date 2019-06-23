package nerdhub.cardinal.components.api.util.component;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

/**
 * A component that stores data in a {@link CompoundTag}.
 */
public interface NbtStorageComponent extends Component {

    CompoundTag getData();

    final class Impl implements NbtStorageComponent {
        private CompoundTag data = new CompoundTag();

        @Override
        public CompoundTag getData() {
            return data;
        }

        @Override
        public void fromTag(CompoundTag tag) {
            this.data = tag.getCompound("data");
        }

        @Override
        public CompoundTag toTag(CompoundTag tag) {
            tag.put("data", this.data);
            return tag;
        }

        @Override
        public NbtStorageComponent newInstance() {
            return new Impl();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Impl impl = (Impl) o;
            return Objects.equals(data, impl.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }
    }
}
