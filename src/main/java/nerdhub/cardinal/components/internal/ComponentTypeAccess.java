package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.util.Identifier;

public interface ComponentTypeAccess {
    <T extends Component> ComponentType<T> create(Identifier id, Class<T> componentClass, int rawId);
}
