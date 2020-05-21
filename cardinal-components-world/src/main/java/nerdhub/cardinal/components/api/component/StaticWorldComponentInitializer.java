package nerdhub.cardinal.components.api.component;

import net.minecraft.world.World;

/**
 * Entrypoint getting invoked to register <em>static</em> world component factories.
 *
 * <p>The entrypoint, like every {@link StaticComponentInitializer}, is exposed as
 * {@code cardinal-components:static-init} in the mod json and runs for any environment.
 * It usually executes right before the first {@link World} instance is created.
 *
 * @since 2.4.0
 */
public interface StaticWorldComponentInitializer extends StaticComponentInitializer {
    void registerWorldComponentFactories(WorldComponentFactoryRegistry registry);
}
