package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.item.ItemStack;

public interface ItemCaller {
	Event<ItemComponentCallback> cardinal_getItemComponentEvent();

	ComponentContainer cardinal_createComponents(ItemStack stack);
}
