package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import net.fabricmc.fabric.api.event.Event;

public interface ItemCaller {
	Event<ItemComponentCallback> getItemComponentEvent();
}
