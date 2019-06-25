package nerdhub.cardinal.api.event;

@FunctionalInterface
public interface ComponentGatherer<T> extends BiFunction<T, ComponentContainer> {
    void initComponents(T object, ComponentContainer cc);

    default ComponentGatherer<T> andThen(ComponentGatherer after) {
        return (object, cc) -> {
            this.initComponents(object, cc);
            after.initComponents(object, cc);
        }
    }
    
}