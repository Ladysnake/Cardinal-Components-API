package nerdhub.cardinal.components.api.component.trait;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;

public interface TypeAwareComponent extends Component {
    ComponentType<?> getComponentType();
}
