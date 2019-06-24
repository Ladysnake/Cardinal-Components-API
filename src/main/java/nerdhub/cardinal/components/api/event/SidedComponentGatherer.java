package nerdhub.cardinal.api.event;

@FunctionalInterface
public interface SidedComponentGatherer<T> {
    void gatherComponents(T object, ComponentContainer cc);
}
