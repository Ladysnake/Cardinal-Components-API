package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.util.Identifier;

/**
 * @since 2.4.0
 */
public interface WorldComponentFactoryRegistry {
    /**
     * Registers a {@link WorldComponentFactory}.
     *
     * @param componentId the id of a {@link ComponentType}
     * @param factory     the factory to use to create components of the given type
     */
    void register(Identifier componentId, WorldComponentFactory<?> factory);
}
