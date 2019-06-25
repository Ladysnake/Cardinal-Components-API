package nerdhub.cardinal.components.api.util.component.provider;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.provider.ComponentProvider;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * A provider that never exposes any component
 */
public final class EmptyComponentProvider implements ComponentProvider {
    private static final ComponentProvider EMPTY_PROVIDER = new EmptyComponentProvider();

    public static ComponentProvider instance() {
        return EMPTY_PROVIDER;
    }

    /**
     * {@inheritDoc}
     * @return {@code false}
     */
    @Override
    public boolean hasComponent(ComponentType<?> type) {
        return false;
    }

    /**
     * {@inheritDoc}
     * @return {@code null}
     */
    @Nullable
    @Override
    public Component getComponent(ComponentType<?> type) {
        return null;
    }

    /**
     * {@inheritDoc}
     * @return an empty set representing this provider's supported component types
     */
    public Set<ComponentType<? extends Component>> getComponentTypes() {
        return Collections.emptySet();
    }

    private EmptyComponentProvider() {}
}