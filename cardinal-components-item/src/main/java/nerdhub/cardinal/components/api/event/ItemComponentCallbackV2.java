/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 OnyxStudios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package nerdhub.cardinal.components.api.event;

import dev.onyxstudios.cca.api.v3.component.item.ItemComponentFactoryV2;
import dev.onyxstudios.cca.internal.item.CardinalItemInternals;
import dev.onyxstudios.cca.internal.item.ItemCaller;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.util.ItemComponent;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

/**
 * The callback interface for receiving component initialization events
 * during {@code ItemStack} construction.
 *
 * <p> The element that is interested in attaching components
 * to item stacks implements this interface, and is registered
 * with an item's event, using {@link Event#register(Object)}.
 * When a stack of that item is constructed, the callback's
 * {@code initComponents} method is invoked.
 *
 * <p> Unlike {@code EntityComponentCallback}, item component callbacks are
 * registered per item object, and apply to stacks of that item.
 * More formally, if a callback is registered for an item {@code i},
 * its {@code initComponents} method will be invoked for any stack {@code s}
 * verifying {@code s.getItem() == i}.
 */
@FunctionalInterface
public interface ItemComponentCallbackV2 {

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
     * of the given item.
     */
    static Event<ItemComponentCallbackV2> event(@Nullable Item item) {
        return item == null ? CardinalItemInternals.WILDCARD_ITEM_EVENT_V2 : ((ItemCaller) item).cardinal_getItemComponentEventV2();
    }

    @ApiStatus.Experimental
    static <C extends Component> void register(ComponentType<C> type, @Nullable Item item, ItemComponentFactoryV2<? extends C> factory) {
        event(item).register((it, stack, components) -> {
            CopyableComponent<?> cc = factory.createForStack(it, stack);
            if (cc != null) {
                components.put(type, cc);
            }
        });
    }

    /**
     * Initialize components for the given item stack.
     * Components that are added to the given container will be available
     * on the stack as soon as all callbacks have been invoked.
     *
     * <p><b>Some stacks may be initialized with a count of 0, causing {@link ItemStack#getItem()} to
     * return {@link net.minecraft.item.Items#AIR}.</b> Use the provided {@code item} argument,
     * reflecting the real item the stack was constructed with, to run any check.
     *
     * <p> Example code: <pre><code>
     *  ItemComponentCallback.event(Items.DIAMOND_PICKAXE)
     *      .register((item, stack, components) -> components.put(TYPE, new MyComponent()));
     * </code></pre>
     *
     * @param item       the stack's true item.
     * @param stack      the {@code ItemStack} being constructed
     * @param components the stack's component container
     * @implNote Because this method is called for each stack creation, implementations
     * should avoid side effects and keep costly computations at a minimum. Lazy initialization
     * should be considered for components that are costly to initialize.
     * @see ItemComponent
     */
    void initComponents(Item item, ItemStack stack, ComponentContainer<CopyableComponent<?>> components);

}
