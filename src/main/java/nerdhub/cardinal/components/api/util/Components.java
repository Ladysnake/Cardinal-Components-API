package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.provider.ComponentProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.Set;

public final class Components {
    private Components() { throw new AssertionError(); }


    public static boolean areComponentsEqual(ItemStack stack1, ItemStack stack2) {
        if(stack1.isEmpty() && stack2.isEmpty()) {
            return true;
        }
        ComponentProvider accessor = ComponentProvider.fromItemStack(stack1);
        ComponentProvider other = ComponentProvider.fromItemStack(stack2);
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
        ret.deserialize(toCopy.serialize(new CompoundTag()));
        return ret;
    }

}
