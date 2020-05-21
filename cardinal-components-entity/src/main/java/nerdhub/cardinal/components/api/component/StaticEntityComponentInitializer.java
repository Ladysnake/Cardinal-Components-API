package nerdhub.cardinal.components.api.component;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

/**
 * Entrypoint getting invoked to register <em>static</em> entity component factories.
 *
 * <p>The entrypoint, like every {@link StaticComponentInitializer}, is exposed as
 * {@code cardinal-components:static-init} in the mod json and runs for any environment.
 * It usually executes right before the first {@link Entity} instance is created.
 *
 * @since 2.4.0
 */
@ApiStatus.Experimental
public interface StaticEntityComponentInitializer extends StaticComponentInitializer {
    /**
     * Called to register component factories for statically declared component types.
     *
     * <p><strong>The passed registry must not be held onto!</strong> Static component factories
     * must not be registered outside of this method.
     *
     * @param registry an {@link EntityComponentFactoryRegistry} for <em>statically declared</em> components
     */
    void registerEntityComponentFactories(EntityComponentFactoryRegistry registry);
}
