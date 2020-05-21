package nerdhub.cardinal.components.api.component;

import net.minecraft.class_5217;
import org.jetbrains.annotations.ApiStatus;

/**
 * Entrypoint getting invoked to register <em>static</em> item component factories.
 *
 * <p>The entrypoint, like every {@link StaticComponentInitializer}, is exposed as
 * {@code cardinal-components:static-init} in the mod json and runs for any environment.
 * It usually executes right before the first {@linkplain class_5217 save properties object} gets loaded.
 *
 * @since 2.4.0
 */
@ApiStatus.Experimental
public interface StaticLevelComponentInitializer extends StaticComponentInitializer {
    /**
     * Called to register component factories for statically declared component types.
     *
     * <p><strong>The passed registry must not be held onto!</strong> Static component factories
     * must not be registered outside of this method.
     *
     * @param registry a {@link LevelComponentFactoryRegistry} for <em>statically declared</em> components
     */
    void registerLevelComponentFactories(LevelComponentFactoryRegistry registry);
}
