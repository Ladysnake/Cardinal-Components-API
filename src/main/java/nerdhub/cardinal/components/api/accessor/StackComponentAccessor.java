package nerdhub.cardinal.components.api.accessor;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.ItemComponentProvider;
import nerdhub.cardinal.components.api.component.ItemComponent;
import net.minecraft.item.ItemStack;

import java.util.Set;

/**
 * used to access an {@link ItemStack}'s components.
 * if you want to expose components see {@link ItemComponentProvider}
 */
public interface StackComponentAccessor {

    /**
     * if this method returns {@code true}, then {@link #getComponent(ComponentType)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     */
    <T extends ItemComponent> boolean hasComponent(ComponentType<T> type);

    /**
     * @return an instance of the requested component, or {@code null}
     */
    <T extends ItemComponent> T getComponent(ComponentType<T> type);

    /**
     * @return an unmodifiable view of the component types
     */
    Set<ComponentType<? extends ItemComponent>> getComponentTypes();
}
