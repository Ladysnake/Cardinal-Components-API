package nerdhub.cardinal.components.api.component.trait;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;

public interface CloneableComponent<C extends CloneableComponent> extends Component {
    C newInstance();

    default C cloneComponent() {
        C clone = this.newInstance();
        clone.fromTag(this.toTag(new CompoundTag()));
        return clone;
    }
}
