package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import nerdhub.cardinal.components.api.provider.ItemComponentProvider;
import nerdhub.cardinal.components.internal.ItemCaller;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public abstract class MixinItem implements ItemComponentProvider, ItemCaller {
    @Unique
    @SuppressWarnings("unchecked")
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

    @Override
    public void createComponents(ItemStack stack) {
        //NO-OP
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public <T extends Component> void addComponent(ItemStack stack, ComponentType<T> type, T component) {
        // mixin classes can be referenced from other mixins
        //noinspection ReferenceToMixin
        ((MixinItemStack) (Object) stack).setComponentValue(type, component);
    }
}
