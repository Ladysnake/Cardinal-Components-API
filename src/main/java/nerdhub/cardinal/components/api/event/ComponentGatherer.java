package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.container.ComponentContainer;

@FunctionalInterface
public interface ComponentGatherer<T> {
    void initComponents(T object, ComponentContainer cc);

    default ComponentGatherer<T> andThen(ComponentGatherer<T> after) {
        return (object, cc) -> {
            this.initComponents(object, cc);
            after.initComponents(object, cc);
        };
    }
    
}