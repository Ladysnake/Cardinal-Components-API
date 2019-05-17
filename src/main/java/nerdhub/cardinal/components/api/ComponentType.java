package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.util.Identifier;

public class ComponentType<T extends Component> {

    private final Identifier id;

    /**
     * package-private;
     *
     * @see ComponentRegistry#getOrCreate(Class)!
     */
    ComponentType(Class clazz, Identifier id) {
        this.id = id;
    }

    /**
     * convenience method to easily cast a component instance to it's type
     */
    @SuppressWarnings("unchecked")
    public T cast(Object instance) {
        return (T) instance;
    }

    public Identifier getID() {
        return this.id;
    }
}
