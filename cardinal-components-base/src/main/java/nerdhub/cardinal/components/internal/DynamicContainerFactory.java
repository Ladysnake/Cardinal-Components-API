package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;

/**
 * A container factory that takes a single argument, and populates containers dynamically by firing events.
 *
 * <p>Implementations of this interface are typically generated at runtime.
 *
 * @param <T> the type of the single argument used to initialize the container
 * @param <C> a possibly specialized component type stored in created containers
 */
public interface DynamicContainerFactory<T, C extends Component> {
    ComponentContainer<C> create(T obj);
}
