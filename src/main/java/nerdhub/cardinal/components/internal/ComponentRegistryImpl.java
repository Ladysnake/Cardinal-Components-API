package nerdhub.cardinal.components.internal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class ComponentRegistryImpl implements ComponentRegistry {
    
    private final Map<Identifier, ComponentType<?>> registry = new LinkedHashMap<>();
    private final ComponentTypeAccess access;
    private int nextRawId = 0;

    public ComponentRegistryImpl(ComponentTypeAccess access) {
        this.access = access;
    }

    @Override public <T extends Component> ComponentType<T> registerIfAbsent(Identifier componentId, Class<T> componentClass) {
        Preconditions.checkArgument(componentClass.isInterface(), "Base component class must be an interface: " + componentClass.getCanonicalName());
        Preconditions.checkArgument(Component.class.isAssignableFrom(componentClass), "Component interface must extend " + Component.class.getCanonicalName());
        // make sure 2+ components cannot get registered at the same time
        synchronized (registry) {
            // Not using computeIfAbsent since we need to check the possibly registered class first
            @SuppressWarnings("unchecked")
            ComponentType<T> registered = (ComponentType<T>) registry.get(componentId);
            if (registered != null) {
                if (registered.getComponentClass() == componentClass) {
                    throw new IllegalStateException("Registered component " + componentId + " twice with 2 different classes: " + registered.getComponentClass() + ", " + componentClass);
                }
            } else {
                registered = access.create(componentId, componentClass, nextRawId++);
                registry.put(componentId, registered);
                SharedComponentSecrets.registeredComponents = this.registry.values().toArray(new ComponentType[0]);
            }
        }
        return registered;
    }
    
    @Override public ComponentType<?> get(Identifier id) {
        return registry.get(id);
    }

    @Override public Stream<ComponentType<?>> stream() {
        return registry.values().stream();
    }

    @VisibleForTesting
    void clear() {
        this.registry.clear();
    }
}
