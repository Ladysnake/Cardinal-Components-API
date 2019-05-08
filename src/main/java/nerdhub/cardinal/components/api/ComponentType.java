package nerdhub.cardinal.components.api;

public class ComponentType<T> {

    /**
     * package-private;
     *
     * @see ComponentRegistry#get(Class)!
     */
    ComponentType() {
    }

    /**
     * convenience method to easily cast a component instance to it's type
     */
    @SuppressWarnings("unchecked")
    public <V> V cast(Object instance) {
        return (V) instance;
    }
}
