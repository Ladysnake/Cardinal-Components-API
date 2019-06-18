package nerdhub.cardinal.components.impl;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.provider.ComponentProvider;
import com.google.collect.Sets;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * A ComponentProvider that falls back onto another when it is missing a component.
 */
public class FallBackComponentProvider implements ComponentProvider {
    protected ComponentProvider main;
    protected ComponentProvider fallback;

    public FallBackComponentProvider(ComponentContainer main, ComponentProvider fallback) {
        this.main = main;
        this.fallback = fallback;
    }

    /**
     * if this method returns {@code true}, then {@link #getComponent(ComponentType)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     */
    @Override
    public boolean hasComponent(ComponentType<?> type) {
        return main.hasComponent(type) || fallback.hasComponent(type);
    }

    /**
     * A component requester should generally call one of the {@link ComponentType} methods
     * instead of calling this directly.
     *
     * @return an instance of the requested component, or {@code null}
     * @see ComponentType#get(Object)
     * @see ComponentType#maybeGet(Object)
     */
    @Nullable
    @Override
    public Component getComponent(ComponentType<?> type) {
        Component c = main.get(type);
        return c != null ? c : fallback.get(type);
    }

    /**
     * Returns an unmodifiable view of the union of the two component providers
     * backing this {@code FallBackComponentProvider}. The returned set contains 
     * all component types that are contained in either backing set.
     * Iterating over the returned set iterates first over all the component types of
     * the main provider, then over each component type of the fallback provider, 
     * in order, that is not contained in the main one. 
     *
     * @return an unmodifiable view of the component types of both backing providers
     */
    @Override
    public Set<ComponentType<? extends Component>> getComponentTypes() {
        return Sets.union(main.getComponentTypes(), fallback.getComponentTypes());
    }
}


