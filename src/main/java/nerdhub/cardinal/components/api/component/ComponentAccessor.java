package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.ItemComponentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

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
    static ComponentAccessor fromItemStack(ItemStack stack) {
        return (ComponentAccessor) (Object) stack;
    }

    /**
     * convenience method to retrieve a ComponentAccessor from a given {@link Entity}
     */
    static ComponentAccessor fromEntity(Entity entity) {
        return (ComponentAccessor) entity;
    }

    /**
     * if this method returns {@code true}, then {@link #getComponent(ComponentType)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     */
    boolean hasComponent(ComponentType<?> type);

    /**
     * A component requester should generally call one of the {@link ComponentType} methods
     * instead of calling this directly.
     *
     * @return an instance of the requested component, or {@code null}
     * @see ComponentType#get(Object)
     * @see ComponentType#maybeGet(Object)
     */
    Component getComponent(ComponentType<?> type);

    /**
     * @return an unmodifiable view of the component types
     */
    Set<ComponentType<? extends Component>> getComponentTypes();
}
