package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.CardinalEventsInternals;
import nerdhub.cardinal.components.internal.ItemCaller;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * The callback interface for receiving component initialization events
 * during {@code ItemStack} construction. The element that is interested in
 * attaching components to item stacks implements this interface, and is
 * registered with an item's event, using {@link Event#register(Object)}.
 * When a stack of that item is constructed, the callback's
 * {@code initComponents} method is invoked.
 * 
 * <p> Unlike {@link EntityComponentCallback}, item component callbacks are
 * registered per item object, and apply to stacks of that item. 
 * More formally, if a callback is registered for an item {@code i},
 * its {@code initComponents} method will be invoked for any stack {@code s}
 * verifying {@code s.getItem() == i}.
 */
@FunctionalInterface
public interface ItemComponentCallback {

    /**
     * Returns the {@code Event} used to register component callbacks for
     * stacks of the given item.
     *
     * <p> The {@code null} value works as a wildcard parameter. A callback
     * registered to the wilcard event is called for every item stack ever
     * created. For performance reasons, wildcard callbacks should be avoided
     * where possible. Notably, when registering callbacks for various items,
     * it is often better to register a separate specialized callback for each one
     * than a single generic callback with additional checks.
     * 
     * @param item an item, or {@code null} to target every stack.
     * @return the {@code Event} used to register component callbacks for stacks
     *         of the given item.
     */
    static Event<ItemComponentCallback> event(@Nullable Item item) {
        return item == null ? CardinalEventsInternals.WILDCARD_ITEM_EVENT : ((ItemCaller) item).cardinal_getItemComponentEvent();
    }

    /**
     * Convenience method to register an item that implements its own component callback
     *
     * @param item an item that initializes itself the components of its stacks
     * @param <I> the type of the item
     * @return {@code item} for easy chaining
     */
    static <I extends Item & ItemComponentCallback> I registerSelf(I item) {
        event(item).register(item);
        return item;
    }

    /**
     * Initialize components for the given item stack.
     * Components that are added to the given container will be available
     * on the stack as soon as all callbacks have been invoked.
     *
     * <p> Example code: <pre><code>
     *  ItemComponentCallback.event(Items.DIAMOND_PICKAXE)
     *      .register((stack, components) -> components.put(TYPE, new MyComponent()) : null);
     * </code></pre>
     *
     * @param stack      the {@code ItemStack} being constructed
     * @param components the stack's component container
     *
     * @implNote Because this method is called for each stack creation, implementations
     * should avoid side effects and keep costly computations at a minimum. Lazy initialization
     * should be considered for components that are costly to initialize.
     */
    void initComponents(ItemStack stack, ComponentContainer components);

}
