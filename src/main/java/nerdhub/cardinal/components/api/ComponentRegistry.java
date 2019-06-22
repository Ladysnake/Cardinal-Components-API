package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.internal.ComponentRegistryImpl;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface ComponentRegistry {
    ComponentRegistry INSTANCE = new ComponentRegistryImpl(ComponentType::new);

    /**
     * used to obtain a shared instance of {@link ComponentType}
     *
     * @param componentClass the interface of which to obtain a {@link ComponentType}
     * @throws IllegalArgumentException if {@code componentClass} is not an interface
     * @throws IllegalArgumentException if {@code componentClass} does not extend {@link Component}
     * @throws IllegalStateException if a different component class has been registered with the same {@code componentId}
     */
    <T extends Component> ComponentType<T> registerIfAbsent(Identifier componentId, Class<T> componentClass);

    /**
     * Directly retrieves a ComponentType from its id.
     */
    @Nullable
    ComponentType<?> get(Identifier id);

    Stream<ComponentType<?>> stream();
}
