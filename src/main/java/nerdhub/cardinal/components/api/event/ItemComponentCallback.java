package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.ItemCaller;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface ItemComponentCallback<T extends Item> {
    static Event<ItemComponentCallback> event(Item item) {
        return ((ItemCaller)item).getItemComponentEvent();
    }

    void attachComponents(ItemStack stack, ComponentContainer components);

}
