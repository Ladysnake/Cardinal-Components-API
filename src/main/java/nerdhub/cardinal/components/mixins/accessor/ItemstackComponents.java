package nerdhub.cardinal.components.mixins.accessor;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;

public interface ItemstackComponents {

    <T extends Component> void setComponentValue(ComponentType<T> type, Component obj);

}
