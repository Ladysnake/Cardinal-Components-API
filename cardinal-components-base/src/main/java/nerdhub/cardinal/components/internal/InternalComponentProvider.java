package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;

public interface InternalComponentProvider extends ComponentProvider {
    @Override
    default boolean hasComponent(ComponentType<?> type) {
        return this.getComponentContainer().containsKey(type);
    }

    @Nullable
    @Override
    default <C extends Component> C getComponent(ComponentType<C> type) {
        return this.getComponentContainer().get(type);
    }

    @Override
    default Set<ComponentType<?>> getComponentTypes() {
        return Collections.unmodifiableSet(this.getComponentContainer().keySet());
    }

    @Nonnull
    default ComponentContainer<?> getComponentContainer() {
        return ((ComponentContainer<?>) getStaticComponentContainer());
    }

    @Override
    default void forEachComponent(BiConsumer<ComponentType<?>, Component> op) {
        this.getComponentContainer().forEach(op);
    }

    @Nonnull
    @Override
    Object getStaticComponentContainer();
}
