package nerdhub.cardinal.components.api;

public class ComponentType<T> {

    private final String id;

    /**
     * package-private;
     *
     * @see ComponentRegistry#getOrCreate(Class)!
     */
    ComponentType(Class clazz) {
        this.id = clazz.getCanonicalName();
    }

    /**
     * convenience method to easily cast a component instance to it's type
     */
    @SuppressWarnings("unchecked")
    public <V> V cast(Object instance) {
        return (V) instance;
    }

    public String getID() {
        return this.id;
    }
}
