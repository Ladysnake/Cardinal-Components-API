package nerdhub.cardinal.components.util.accessor;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;

public interface ItemstackComponents {

    <T extends Component> void setComponentValue(ComponentType<T> type, Component obj);

}
