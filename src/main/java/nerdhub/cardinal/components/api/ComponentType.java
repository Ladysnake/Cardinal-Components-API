package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.util.Identifier;

public final class ComponentType<T extends Component> {

    final Class<T> componentClass;
    private final Identifier id;
    private final int rawId;

    /**
     * Constructs a new immutable ComponentType
     *
     * @see ComponentRegistry#registerIfAbsent(Identifier, Class)!
     */

    /* package-private */ ComponentType(Identifier id, Class<T> componentClass, int rawId) {
        this.componentClass = componentClass;
        this.id = id;
        this.rawId = rawId;
    }

    /**
     * convenience method to easily cast a component instance to it's type
     */
    public T cast(Object instance) {
        return componentClass.cast(instance);
    }

    public Identifier getId() {
        return this.id;
    }

    public int getRawId() {
        return this.rawId;
    }
}
