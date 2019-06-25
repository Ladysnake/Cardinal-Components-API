package nerdhub.cardinal.components.api.component.provider;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.item.ItemStack;

public interface ItemComponentProvider {

    /**
     * called when creating the components for an ItemStack
     * TODO replace this with an Item-specific event
     */
    void createComponents(ItemStack stack);

    /**
     * used to add a component to an {@link ItemStack}
     */
    default <T extends Component> void addComponent(ItemStack stack, ComponentType<T> type, T component) {
        //NO-OP in interface
    }
}
