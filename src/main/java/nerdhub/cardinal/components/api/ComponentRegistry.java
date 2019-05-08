package nerdhub.cardinal.components.api;

import java.util.IdentityHashMap;
import java.util.Map;

public class ComponentRegistry {

    private static final Map<Class<?>, ComponentType<?>> REGISTRY = new IdentityHashMap<>();

    /**
     * used to obtain a shared instance of {@link nerdhub.cardinal.components.api.ComponentType}
     *
     * @param clazz the interface of which to obtain a {@link ComponentType}
     */
    @SuppressWarnings("unchecked")
    public static <T> ComponentType<T> get(Class<T> clazz) {
        if(!clazz.isInterface()) {
            throw new IllegalArgumentException("class must be an interface: " + clazz.getCanonicalName());
        }
        return (ComponentType<T>) REGISTRY.computeIfAbsent(clazz, aClass -> new ComponentType<>());
    }
}
