package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

/**
 * @since 2.4.0
 */
public interface ItemComponentFactoryRegistry {
    /**
     * Registers an {@link ItemComponentFactory}.
     *
     * <p> A {@code null} {@code componentId} works as a wildcard parameter.
     * A callback registered to the wilcard event is called for every item stack ever
     * created. For performance reasons, wildcard callbacks should be avoided
     * where possible. Notably, when registering callbacks for various items,
     * it is often better to register a separate specialized callback for each one
     * than a single generic callback with additional checks.
     *
     * @param componentId the id of a {@link ComponentType}
     * @param itemId      the id of an item to target, or {@code null} to target every stack.
     * @param factory     the factory to use to create components of the given type
     */
    void register(Identifier componentId, @Nullable Identifier itemId, ItemComponentFactory<?> factory);
}
