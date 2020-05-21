package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

/**
 * Entrypoint getting invoked to declare static component types.
 *
 * <p><b>Implementations of this class must not call {@link ComponentRegistry#registerIfAbsent(Identifier, Class)}</b>,
 * directly or indirectly, until {@link #finalizeStaticBootstrap()} is called. They must also avoid causing the
 * instantiation of a component provider (eg. ItemStack). It is recommended to implement this interface
 * on its own class to avoid running static initializers too early, e.g. because they were referenced in field or method
 * signatures in the same class.
 *
 * <p>The entrypoint is exposed as {@code cardinal-components:static-init} in the mod json
 * and runs for any environment. It usually executes right before the first {@link ComponentType}
 * is created, but can be triggered at any time by another module.
 */
@ApiStatus.Experimental
public interface StaticComponentInitializer {
    /**
     * @return the identifiers of the {@link ComponentType}s this initializer supports
     */
    Set<Identifier> getSupportedComponentTypes();

    /**
     * Called when static component bootstrap is finished.
     *
     * <p>It is safe to register {@link ComponentType}s in this method.
     */
    default void finalizeStaticBootstrap() {
        // NO-OP
    }
}
