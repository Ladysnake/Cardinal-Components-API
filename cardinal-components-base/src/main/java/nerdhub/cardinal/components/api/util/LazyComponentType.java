package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class LazyComponentType {
    public static LazyComponentType create(String id) {
        return create(new Identifier(id));
    }

    public static LazyComponentType create(Identifier id) {
        return new LazyComponentType(id);
    }

    private final Identifier componentId;
    private ComponentType<?> value;

    private LazyComponentType(Identifier componentId) {
        this.componentId = componentId;
    }

    public ComponentType<?> get() {
        if (this.value == null) {
            this.value = Objects.requireNonNull(ComponentRegistry.INSTANCE.get(this.componentId), "The component type for '" + this.componentId  +"'  was not registered");
        }

        return this.value;
    }

    public <T extends Component> ComponentType<T> get(Class<T> type) {
        @SuppressWarnings("unchecked") ComponentType<T> ret = (ComponentType<T>) this.get();
        if (!type.isAssignableFrom(ret.getComponentClass())) {
            throw new IllegalStateException("Queried type " + type + " mismatches with registered type " +ret.getComponentClass());
        }
        return ret;
    }
}
