package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.ItemComponentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.Set;

/**
 * used to access an object's components.
 * if you want to expose components see {@link ItemComponentProvider}
 */
public interface ComponentAccessor {

    /**
     * convenience method to retrieve ComponentAccessor from a given {@link ItemStack}
     */
    @SuppressWarnings("ConstantConditions")
    static ComponentAccessor get(ItemStack stack) {
        return (ComponentAccessor) (Object) stack;
    }

    /**
     * convenience method to retrieve a ComponentAccessor from a given {@link Entity}
     */
    static ComponentAccessor get(Entity entity) {
        return (ComponentAccessor) entity;
    }

    /**
     * if this method returns {@code true}, then {@link #getComponent(ComponentType)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     */
    <T extends Component> boolean hasComponent(ComponentType<T> type);

    /**
     * @return an instance of the requested component, or {@code null}
     */
    <T extends Component> T getComponent(ComponentType<T> type);

    default <T extends Component> Optional<T> optionally(ComponentType<T> type) {
        return Optional.ofNullable(getComponent(type));
    }

    /**
     * @return an unmodifiable view of the component types
     */
    Set<ComponentType<? extends Component>> getComponentTypes();
}
