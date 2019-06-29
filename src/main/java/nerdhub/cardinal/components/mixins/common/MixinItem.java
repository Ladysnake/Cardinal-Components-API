package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import nerdhub.cardinal.components.internal.CardinalEventsInternals;
import nerdhub.cardinal.components.internal.FeedbackContainerFactory;
import nerdhub.cardinal.components.internal.ItemCaller;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public abstract class MixinItem implements ItemCaller {
    @Unique private final Event<ItemComponentCallback> cardinal_componentsEvent = CardinalEventsInternals.createItemComponentsEvent();
    @Unique private FeedbackContainerFactory<ItemStack> cardinal_containerFactory;

    @Override
    public Event<ItemComponentCallback> cardinal_getItemComponentEvent() {
        return this.cardinal_componentsEvent;
    }

    @Override
    public ComponentContainer cardinal_createComponents(ItemStack stack) {
        // assert stack.getItem() == this;
        if (this.cardinal_containerFactory == null) {
            cardinal_containerFactory = new FeedbackContainerFactory<>(
                    CardinalEventsInternals.WILDCARD_ITEM_EVENT,
                    cardinal_componentsEvent
            );
        }
        return this.cardinal_containerFactory.create(stack);
    }
}
