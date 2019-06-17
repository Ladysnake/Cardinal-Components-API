package nerdhub.cardinal.components.impl;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.ItemComponentProvider;

import java.util.Collections;
import java.util.Set;

/**
 * used to access an object's components.
 * if you want to expose components see {@link ItemComponentProvider}
 */
public class EmptyComponentProvider implements ComponentProvider {

    /**
     * if this method returns {@code true}, then {@link #getComponent(ComponentType)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     */
    @Override
    public boolean hasComponent(ComponentType<?> type) {
        return false;
    }

    /**
     * A component requester should generally call one of the {@link ComponentType} methods
     * instead of calling this directly.
     *
     * @return an instance of the requested component, or {@code null}
     * @see ComponentType#get(Object)
     * @see ComponentType#maybeGet(Object)
     */
    @Override
    public Component getComponent(ComponentType<?> type) {
        return null;
    }

    /**
     * @return an unmodifiable view of the component types
     */
    public Set<ComponentType<? extends Component>> getComponentTypes() {
        return Collections.emptySet();
    }

}