package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import nerdhub.cardinal.components.internal.ItemCaller;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public abstract class MixinItem implements ItemCaller {
    @Unique
    private final Event<ItemComponentCallback> cardinal_componentEvent =
            EventFactory.createArrayBacked(ItemComponentCallback.class, callbacks -> (stack, components) -> {
                for (ItemComponentCallback callback : callbacks) {
                    callback.attachComponents(stack, components);
                }
            });

    @Override
    public Event<ItemComponentCallback> getItemComponentEvent() {
        return this.cardinal_componentEvent;
    }
}
