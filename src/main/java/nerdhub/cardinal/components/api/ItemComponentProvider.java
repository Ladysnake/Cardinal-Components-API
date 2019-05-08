package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.ItemComponent;
import net.minecraft.item.ItemStack;

public interface ItemComponentProvider {

    /**
     * called when creating the components for an ItemStack
     */
    void initComponents(ItemStack stack);

    /**
     * used to add a component to an {@link ItemStack}
     */
    default <T extends ItemComponent> void addComponent(ItemStack stack, ComponentType<T> type, T component) {
        //NO-OP in interface
    }
}
