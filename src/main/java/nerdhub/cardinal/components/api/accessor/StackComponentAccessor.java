package nerdhub.cardinal.components.api.accessor;

import nerdhub.cardinal.components.api.BlockComponentProvider;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.ItemComponent;
import net.minecraft.item.ItemStack;

import java.util.Set;

public interface StackComponentAccessor {

    /**
     * if this method returns {@code true}, then {@link #getComponent(ComponentType)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     *
     * @return whether or not this {@link BlockComponentProvider} can provide the desired component
     */
    <T extends ItemComponent> boolean hasComponent(ComponentType<T> type);

    /**
     * @return an instance of the requested component, or {@code null}
     */
    <T extends ItemComponent> T getComponent(ComponentType<T> type);

    /**
     * @return an unmodifiable view of the component types exposed by this {@link BlockComponentProvider}
     */
    Set<ComponentType<? extends ItemComponent>> getComponentTypes(ItemStack stack);
}
