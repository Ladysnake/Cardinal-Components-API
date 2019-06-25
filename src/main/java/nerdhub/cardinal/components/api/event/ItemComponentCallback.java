package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.ItemCaller;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface ItemComponentCallback {

    Event<ItemComponentCallback> EVENT = EventFactory.createArrayBacked(ItemComponentCallback.class,
            (callbacks) -> (item) -> {
                ComponentGatherer<ItemStack> ret = item instanceof ItemComponentGatherer ? (ItemComponentGatherer) item : null;
                for (ItemComponentCallback callback : callbacks) {
                    ComponentGatherer<Item> g = callback.getComponentGatherer(item);
                    if (g != null) {
                        ret = ret == null ? g : ret.andThen(g);
                    }
                }
                return ret == null ? (s, cc) -> {} : ret;
            })

    /**
     * Example code: <pre><code>
     *  ItemComponentCallback.EVENT.register(i -> i == DIAMOND_PICKAXE ? (stack, cc) -> cc.put(TYPE, new MyComponent()) : null);
     * </code></pre>
     */
    @Nullable
    ComponentGatherer<ItemStack> getComponentGatherer(Item item);

}
