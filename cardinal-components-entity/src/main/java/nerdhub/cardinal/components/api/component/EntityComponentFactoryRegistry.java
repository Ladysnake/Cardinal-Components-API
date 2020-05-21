package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 * @since 2.4.0
 */
public interface EntityComponentFactoryRegistry {
    /**
     * Registers an {@link EntityComponentFactory}.
     *
     * <p> Callers of this method should always use the most specific entity
     * type for their use. For example, a factory which goal is to attach a component
     * to players should pass {@code PlayerEntity.class} as a parameter,
     * not one of its superclasses. This limits the need for entity-dependant
     * checks, as well as the amount of redundant callback invocations.
     * For these reasons, when registering factories for various entity types,
     * it is often better to register a separate specialized callback for each type
     * than a single generic callback with additional checks.
     *
     * @param componentId the id of a {@link ComponentType}
     * @param target      a class object representing the type of entities targeted by the factory
     * @param factory     the factory to use to create components of the given type
     * @param <E>         the type of entities targeted by the factory
     */
    <E extends Entity> void register(Identifier componentId, Class<E> target, EntityComponentFactory<?, E> factory);
}
