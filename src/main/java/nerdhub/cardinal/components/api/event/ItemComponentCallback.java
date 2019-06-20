package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.ItemCaller;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.item.Item;

@FunctionalInterface
public interface ItemComponentCallback<T extends Item> {
    @SuppressWarnings("unchecked")
    static <T extends Item> Event<ItemComponentCallback<T>> event(T item) {
        return (Event<ItemComponentCallback<T>>) (Event) ((ItemCaller)item).getItemComponentEvent();
    }

    void attachComponents(T item, ComponentContainer components);

}
