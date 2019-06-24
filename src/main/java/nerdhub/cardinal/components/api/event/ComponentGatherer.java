package nerdhub.cardinal.api.event;

@FunctionalInterface
public interface ComponentGatherer<T> extends BiFunction<T, ComponentContainer> {
    @Override
    void apply(T object, ComponentContainer cc);
}