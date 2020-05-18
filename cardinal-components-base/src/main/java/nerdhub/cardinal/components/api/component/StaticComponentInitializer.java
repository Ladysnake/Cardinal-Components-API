package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

/**
 * <b>Implementations of this class must not call {@link ComponentRegistry#registerIfAbsent(Identifier, Class)}</b>,
 * directly or indirectly, until {@link #finalizeStaticBootstrap()} is called.
 */
@ApiStatus.Experimental
public interface StaticComponentInitializer {
    /**
     *
     * @return the identifiers of the {@link ComponentType}s this initializer supports
     */
    Set<Identifier> getSupportedComponentTypes();

    /**
     * Called when static component bootstrap is finished.
     *
     * <p>It is safe to retrieve {@link ComponentType}s in this method.
     */
    default void finalizeStaticBootstrap() {
        // NO-OP
    }
}
