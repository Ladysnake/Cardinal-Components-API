package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class ComponentRegistry {

    private static final Map<Identifier, ComponentType<?>> REGISTRY = new HashMap<>();
    private static int nextRawId = 0;

    /**
     * used to obtain a shared instance of {@link ComponentType}
     *
     * @param componentClass the interface of which to obtain a {@link ComponentType}
     */
    public static <T extends Component> ComponentType<T> registerIfAbsent(Identifier componentId, Class<T> componentClass) {
        @SuppressWarnings("unchecked")
        ComponentType<T> registered = (ComponentType<T>) REGISTRY.get(componentId);
        if(!componentClass.isInterface()) {
            throw new IllegalArgumentException("Base component class must be an interface: " + componentClass.getCanonicalName());
        } else if(registered != null && registered.componentClass != componentClass) {
            throw new IllegalStateException("Registered component " + componentId + " twice with 2 different classes: " + registered.componentClass + ", " + componentClass);
        } else if(registered == null) {
            // Not using computeIfAbsent since we need to check the possibly registered class first
            registered = new ComponentType<>(componentId, componentClass, nextRawId++);
            REGISTRY.put(componentId, registered);
        }
        return registered;
    }
    
    /**
     * Directly retrieves a ComponentType from its id.
     */
    public static ComponentType<?> get(Identifier id) {
        return REGISTRY.get(id);
    }

    private ComponentRegistry() { throw new AssertionError(); }
}
