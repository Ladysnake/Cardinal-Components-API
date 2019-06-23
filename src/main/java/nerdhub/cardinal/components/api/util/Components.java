package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.provider.ComponentProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.Set;

public final class Components {
    private Components() { throw new AssertionError(); }

    /**
     * Checks item stack equality based on their exposed components.
     *
     * <p> Two {@link ItemStack#isEmpty empty} item stacks will be considered
     * equal, as they would expose no component.
     */
    public static boolean areComponentsEqual(ItemStack stack1, ItemStack stack2) {
        return (stack1.isEmpty() && stack2.isEmpty()) || areComponentsEqual(ComponentProvider.fromItemStack(stack1), ComponentProvider.fromItemStack(stack2));
    }

    /**
     * Compares a provider with another for equality based on the components they expose.
     * Returns {@code true} if the two providers expose the same component types through
     * {@link ComponentProvider#getComponentTypes}, and, for each of the types exposed as such,
     * the corresponding component values are equal according to {@link Component#isComponentEqual}.
     */
    public static boolean areComponentsEqual(ComponentProvider accessor, ComponentProvider other) {
        Set<ComponentType<? extends Component>> types = accessor.getComponentTypes();
        if(types.size() == other.getComponentTypes().size()) {
            for(ComponentType<? extends Component> type : types) {
                if(!other.hasComponent(type) || !accessor.getComponent(type).isComponentEqual(other.getComponent(type))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static Component copyOf(Component toCopy) {
        Component ret = toCopy.newInstance();
        ret.fromTag(toCopy.toTag(new CompoundTag()));
        return ret;
    }

}
