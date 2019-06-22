package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.internal.ComponentRegistryImpl;
import net.minecraft.util.Identifier;

import java.util.stream.Stream;

public interface ComponentRegistry {
    ComponentRegistry INSTANCE = new ComponentRegistryImpl(ComponentType::new);

    /**
     * used to obtain a shared instance of {@link ComponentType}
     *
     * @param componentClass the interface of which to obtain a {@link ComponentType}
     */
    <T extends Component> ComponentType<T> registerIfAbsent(Identifier componentId, Class<T> componentClass);

    /**
     * Directly retrieves a ComponentType from its id.
     */
    ComponentType<?> get(Identifier id);

    Stream<ComponentType<?>> stream();
}
