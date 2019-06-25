package nerdhub.cardinal.api.event;

@FunctionalInterface
public interface SidedComponentGatherer<T> {
    void initComponents(T object, ComponentContainer cc);

    default SidedComponentGatherer<T> andThen(SidedComponentGatherer after) {
        return (object, cc) -> {
            this.initComponents(object, cc);
            after.initComponents(object, cc);
        }
    }
}
