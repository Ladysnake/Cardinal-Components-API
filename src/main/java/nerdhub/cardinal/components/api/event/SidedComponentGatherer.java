package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.container.SidedContainerCompound;

@FunctionalInterface
public interface SidedComponentGatherer<T> {
    void initComponents(T object, SidedContainerCompound cc);

    default SidedComponentGatherer<T> andThen(SidedComponentGatherer<T> after) {
        return (object, cc) -> {
            this.initComponents(object, cc);
            after.initComponents(object, cc);
        };
    }
}
