package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.internal.ComponentRegistryImpl;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * A registry for components.
 * 
 * <p> A {@code ComponentRegistry} is used for registering components and obtaining 
 * {@link ComponentType} instances serving as keys for those components.
 *
 * @see Component
 * @see ComponentType
 */
public interface ComponentRegistry {
    /** The component registry */
    ComponentRegistry INSTANCE = new ComponentRegistryImpl(ComponentType::new);

    /**
     * Registers a component type for the given identifier and class, and returns
     * a shared {@link ComponentType} representation.
     *
     * <p> Calling this method multiple times with the same parameters has the same effect
     * as calling {@link #get(Identifier)} after the first registration call.
     * Calling this method multiple times with the same id but different component classes
     * is forbidden and will throw an {@link IllegalStateException}.
     *
     * @param componentId a unique identifier for the registered component type
     * @param componentClass the interface of which to obtain a {@link ComponentType}
     * @return a shared instance of {@link ComponentType}
     * @throws IllegalArgumentException if {@code componentClass} is not an interface
     * @throws IllegalArgumentException if {@code componentClass} does not extend {@link Component}
     * @throws IllegalStateException if a different component class has been registered with the same {@code componentId}
     */
    <T extends Component> ComponentType<T> registerIfAbsent(Identifier componentId, Class<T> componentClass);

    /**
     * Directly retrieves a ComponentType using its id.
     *
     * @return the {@code ComponentType} that got registered with {@code id}, or {@code null}
     *         if no such {@code ComponentType} is found.
     */
    @Nullable
    ComponentType<?> get(Identifier id);

    /**
     * Return a sequential stream with this registry at its source.
     *
     * @return a sequential {@code Stream} over the component types of this registry.
     */
    Stream<ComponentType<?>> stream();
}
