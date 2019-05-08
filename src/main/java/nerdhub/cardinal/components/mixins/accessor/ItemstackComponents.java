package nerdhub.cardinal.components.mixins.accessor;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.ItemComponent;

public interface ItemstackComponents {

    <T extends ItemComponent> void addComponent(ComponentType<T> type, T obj);
}
