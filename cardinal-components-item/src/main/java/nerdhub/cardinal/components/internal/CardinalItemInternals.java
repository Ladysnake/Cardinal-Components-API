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
package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.InvocationTargetException;

public final class CardinalItemInternals {
    public static final Event<ItemComponentCallback> WILDCARD_ITEM_EVENT = createItemComponentsEvent();

    public static Event<ItemComponentCallback> createItemComponentsEvent() {
        return EventFactory.createArrayBacked(ItemComponentCallback.class,
            (listeners) -> (stack, components) -> {
                for (ItemComponentCallback listener : listeners) {
                    listener.initComponents(stack, components);
                }
            });
    }

    /**
     * Creates a container factory for an item id.
     *
     * <p>The container factory will populate the container by invoking the event for that item
     * as well as the {@linkplain #WILDCARD_ITEM_EVENT wildcard event}.
     */
    public static FeedbackContainerFactory<ItemStack, CopyableComponent<?>> createItemStackContainerFactory(Item item) {
        Identifier itemId = Registry.ITEM.getId(item);
        Class<?> factoryClass = StaticItemComponentPlugin.INSTANCE.getFactoryClass(itemId.toString());
        @SuppressWarnings("unchecked") Event<ItemComponentCallback>[] componentEvents = new Event[] {WILDCARD_ITEM_EVENT, ((ItemCaller) item).cardinal_getItemComponentEvent()};
        if (factoryClass == null) {
            return new FeedbackContainerFactory<>(componentEvents);
        }
        try {
            @SuppressWarnings("unchecked") FeedbackContainerFactory<ItemStack, CopyableComponent<?>> ret = (FeedbackContainerFactory<ItemStack, CopyableComponent<?>>) factoryClass.getConstructor(Event[].class).newInstance((Object) componentEvents);
            return ret;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new StaticComponentLoadingException("Failed to instantiate generated component factory", e);
        }
    }

    @SuppressWarnings({"ConstantConditions", "unchecked", "rawtypes"})
    public static void copyComponents(ItemStack original, ItemStack copy) {
        ComponentContainer<CopyableComponent<?>> other = ((ItemStackAccessor) (Object) copy).cardinal_getComponentContainer();
        ((ItemStackAccessor) (Object) original).cardinal_getComponentContainer().forEach((type, component) -> {
                CopyableComponent ccp = (CopyableComponent) other.get(type);
                if (ccp != null) {
                    ccp.copyFrom(component);
                }
            }
        );
    }
}
