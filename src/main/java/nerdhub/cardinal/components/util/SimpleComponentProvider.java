package nerdhub.cardinal.components.util;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.provider.ItemComponentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.Set;

/**
 * used to access an object's components.
 */
public class SimpleComponentProvider {
    protected ComponentContainer backing;

    public SimpleComponentProvider(ComponentContainer backing) {
        this.backing = backing;
    }

    /**
     * if this method returns {@code true}, then {@link #getComponent(ComponentType)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     */
    @Override
    public boolean hasComponent(ComponentType<?> type) {
        return backing.containsKey(type);
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
        return backing.get(type);
    }

    /**
     * @return an unmodifiable view of the component types
     */
    @Override
    public Set<ComponentType<? extends Component>> getComponentTypes() {
        return Collections.unmodifiableSet(backing.keySet());
    }
}

