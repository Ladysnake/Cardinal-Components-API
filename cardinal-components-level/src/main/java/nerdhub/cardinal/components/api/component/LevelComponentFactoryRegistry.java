package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * @since 2.4.0
 */
@ApiStatus.Experimental
public interface LevelComponentFactoryRegistry {
    /**
     * Registers a {@link LevelComponentFactory}.
     *
     * @param componentId the id of a {@link ComponentType}
     * @param factory     the factory to use to create components of the given type
     */
    void register(Identifier componentId, LevelComponentFactory<?> factory);
}
