package nerdhub.cardinal.components.api.component;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * Entrypoint getting invoked to register <em>static</em> item component factories.
 *
 * <p>The entrypoint, like every {@link StaticComponentInitializer}, is exposed as
 * {@code cardinal-components:static-init} in the mod json and runs for any environment.
 * It usually executes right before the first {@link ItemStack} instance is created.
 *
 * @since 2.4.0
 */
@ApiStatus.Experimental
public interface StaticItemComponentInitializer extends StaticComponentInitializer {
    /**
     * Called to register component factories for statically declared component types.
     *
     * <p><strong>The passed registry must not be held onto!</strong> Static component factories
     * must not be registered outside of this method.
     *
     * @param registry an {@link ItemComponentFactoryRegistry} for <em>statically declared</em> components
     */
    void registerItemComponentFactories(ItemComponentFactoryRegistry registry);
}
