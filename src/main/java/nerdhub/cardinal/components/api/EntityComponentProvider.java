package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.Component;

import java.util.Map;

public interface EntityComponentProvider {

    /**
     * called when creating the components for an Entity<br/>
     * simply add your components to the map
     */
    void createComponents(Map<ComponentType<? extends Component>, Component> components);

}
