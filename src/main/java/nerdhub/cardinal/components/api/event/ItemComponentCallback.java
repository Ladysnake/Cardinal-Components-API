package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.util.gatherer.ItemComponentGatherer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

@FunctionalInterface
public interface ItemComponentCallback {

    Event<ItemComponentCallback> EVENT = EventFactory.createArrayBacked(ItemComponentCallback.class,
            (callbacks) -> (item) -> {
                ComponentGatherer<ItemStack> ret = item instanceof ItemComponentGatherer ? (ItemComponentGatherer) item : null;
                for (ItemComponentCallback callback : callbacks) {
                    ComponentGatherer<ItemStack> gatherer = callback.getComponentGatherer(item);
                    if (gatherer != null) {
                        ret = ret == null ? gatherer : ret.andThen(gatherer);
                    }
                }
                return ret == null ? (s, cc) -> {} : ret;
            });

    /**
     * Example code: <pre><code>
     *  ItemComponentCallback.EVENT.register(i -> i == DIAMOND_PICKAXE ? (stack, cc) -> cc.put(TYPE, new MyComponent()) : null);
     * </code></pre>
     */
    @Nullable
    ComponentGatherer<ItemStack> getComponentGatherer(Item item);

}
