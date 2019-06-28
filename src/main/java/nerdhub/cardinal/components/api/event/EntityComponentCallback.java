package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.CardinalEventsInternals;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.entity.Entity;

/**
 * The callback interface for receiving component initialization events
 * during entity construction. The element that is interested in attaching
 * components to entities implements this interface, and is
 * registered with a class' event, using {@link Event#register(Object)}.
 * When an entity of an applicable type is constructed, the callback's
 * {@code initComponents} method is invoked.
 * 
 * <p> Entity component callbacks are registered per entity class, and apply to 
 * instances of that class and of every subclass. More formally, if a callback
 * is registered for a class {@code E}, its {@code initComponents} method will
 * be invoked for any entity {@code e} verifying {@code e instanceof E}.
 *
 * @param <E> the type of entity targeted by this callback
 */
@FunctionalInterface
public interface EntityComponentCallback<E extends Entity> {

    /**
     * Returns the {@code Event} used to register component callbacks for
     * entities of the given type.
     *
     * <p> Callers of this method should always use the most specific entity
     * type for their use. For example, a callback which goal is to attach a component
     * to players should pass {@code PlayerEntity.class} as a parameter,
     * not one of its superclasses. This limits the need for entity-dependant
     * checks, as well as the amount of redundant callback invocations.
     * For these reasons, when registering callbacks for various entity types,
     * it is often better to register a separate specialized callback for each type
     * than a single generic callback with additional checks.
     * 
     * @param clazz The class object representing the desired entity type
     * @param <E>   The type of entities targeted by the event
     * @return the {@code Event} used to register component callbacks for entities
     *         of the given type.
     * @throws IllegalArgumentException if {@code clazz} is not an entity class
     */
    static <E extends Entity> Event<EntityComponentCallback<E>> event(Class<E> clazz) {
        return CardinalEventsInternals.event(clazz);
    }

    /**
     * Initialize components for the given entity.
     * Components that are added to the given container will be available
     * on the entity as soon as all callbacks have been invoked.
     *
     * @param entity     the entity being constructed
     * @param components the entity's component container
     *
     * @implNote Because this method is called for each entity creation, implementations
     * should avoid side effects and keep costly computations at a minimum. Lazy initialization
     * should be considered for components that are costly to initialize.
     */
    void initComponents(E entity, ComponentContainer components);
}
