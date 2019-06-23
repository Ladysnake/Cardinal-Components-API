package nerdhub.cardinal.components;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

public interface TestComponent extends Component {

    class Impl implements TestComponent {
        private int i;

        public Impl(int i) {
            this.i = i;
        }

        @Override
        public void fromTag(CompoundTag tag) {
            this.i = tag.getInt("i");
        }

        @Override
        public CompoundTag toTag(CompoundTag tag) {
            tag.putInt("i", i);
            return tag;
        }

        @Override
        public Component newInstance() {
            return new Impl(i);
        }

        @Override
        public String toString() {
            return "Impl@" + this.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Impl impl = (Impl) o;
            return i == impl.i;
        }

        @Override
        public int hashCode() {
            return Objects.hash(i);
        }
    }
}
