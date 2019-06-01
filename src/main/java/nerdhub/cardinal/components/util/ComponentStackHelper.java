package nerdhub.cardinal.components.util;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.accessor.StackComponentAccessor;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.Set;

public class ComponentStackHelper {

    @SuppressWarnings("ConstantConditions")
    public static boolean areComponentsEqual(ItemStack stack1, ItemStack stack2) {
        if(stack1.isEmpty() && stack2.isEmpty()) {
            return true;
        }
        StackComponentAccessor accessor = (StackComponentAccessor) (Object) stack1;
        StackComponentAccessor other = (StackComponentAccessor) (Object) stack2;
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
        Component ret = toCopy.newInstanceForItemStack();
        ret.fromItemTag(toCopy.toItemTag(new CompoundTag()));
        return ret;
    }
}
