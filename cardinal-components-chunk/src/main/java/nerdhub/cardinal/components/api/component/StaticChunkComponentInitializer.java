package nerdhub.cardinal.components.api.component;

import net.minecraft.world.chunk.Chunk;

/**
 * Entrypoint getting invoked to register <em>static</em> chunk component factories.
 *
 * <p>The entrypoint, like every {@link StaticComponentInitializer}, is exposed as
 * {@code cardinal-components:static-init} in the mod json and runs for any environment.
 * It usually executes right before the first {@link Chunk} instance is created.
 *
 * @since 2.4.0
 */
public interface StaticChunkComponentInitializer extends StaticComponentInitializer {
    /**
     * Called to register component factories for statically declared component types.
     *
     * <p><strong>The passed registry must not be held onto!</strong> Static component factories
     * must not be registered outside of this method.
     *
     * @param registry a {@link ChunkComponentFactoryRegistry} for <em>statically declared</em> components
     */
    void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry);
}
