package nerdhub.cardinal.components.api.util.component;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.trait.CloneableComponent;

public interface ItemComponent extends Component, CloneableComponent<ItemComponent> {
    @Override
    boolean isComponentEqual(Component other);
}
