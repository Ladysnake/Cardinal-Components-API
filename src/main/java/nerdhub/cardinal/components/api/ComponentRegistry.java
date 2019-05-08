package nerdhub.cardinal.components.api;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class ComponentRegistry {

    private static final Map<Class<?>, ComponentType<?>> REGISTRY = new IdentityHashMap<>();
    private static final Map<String, ComponentType<?>> NAME_LOOKUP = new HashMap<>();

    /**
     * used to obtain a shared instance of {@link nerdhub.cardinal.components.api.ComponentType}
     *
     * @param clazz the interface of which to obtain a {@link ComponentType}
     */
    @SuppressWarnings("unchecked")
    public static <T> ComponentType<T> getOrCreate(Class<T> clazz) {
        if(!clazz.isInterface()) {
            throw new IllegalArgumentException("class must be an interface: " + clazz.getCanonicalName());
        }
        return (ComponentType<T>) REGISTRY.computeIfAbsent(clazz, aClass -> {
            ComponentType<T> type = new ComponentType<>(aClass);
            NAME_LOOKUP.put(type.getID(), type);
            return type;
        });
    }

    /**
     * internal use ONLY
     */
    @SuppressWarnings("unchecked")
    public static <T> ComponentType<T> get(String id) {
        return (ComponentType<T>) NAME_LOOKUP.get(id);
    }
}
