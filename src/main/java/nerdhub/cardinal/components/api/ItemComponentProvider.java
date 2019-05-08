package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.ItemComponent;
import net.minecraft.item.ItemStack;

public interface ItemComponentProvider {

    void initComponents(ItemStack stack);

    default <T extends ItemComponent> void addComponent(ItemStack stack, ComponentType<T> type, T component) {
        //NO-OP in interface
    }
}
