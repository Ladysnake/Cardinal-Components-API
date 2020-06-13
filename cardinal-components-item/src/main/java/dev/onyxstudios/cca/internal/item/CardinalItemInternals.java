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
package dev.onyxstudios.cca.internal.item;

import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import dev.onyxstudios.cca.internal.base.InternalComponentProvider;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import nerdhub.cardinal.components.api.event.ItemComponentCallbackV2;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.Map;

public final class CardinalItemInternals {
    public static final Event<ItemComponentCallbackV2> WILDCARD_ITEM_EVENT_V2 = createItemComponentsEventV2();
    public static final Event<ItemComponentCallback> WILDCARD_ITEM_EVENT = createItemComponentsEvent(WILDCARD_ITEM_EVENT_V2);

    public static Event<ItemComponentCallbackV2> createItemComponentsEventV2() {
        return EventFactory.createArrayBacked(ItemComponentCallbackV2.class,
            (listeners) -> (item, stack, components) -> {
                for (ItemComponentCallbackV2 listener : listeners) {
                    listener.initComponents(item, stack, components);
                }
            });
    }

    public static Event<ItemComponentCallback> createItemComponentsEvent(Event<ItemComponentCallbackV2> proxied) {
        Event<ItemComponentCallback> ret = EventFactory.createArrayBacked(ItemComponentCallback.class,
            (listeners) -> (stack, components) -> {
                for (ItemComponentCallback listener : listeners) {
                    listener.initComponents(stack, components);
                }
            });
        proxied.register((item, stack, components) -> ret.invoker().initComponents(stack, components));
        return ret;
    }

    /**
     * Creates a container factory for an item id.
     *
     * <p>The container factory will populate the container by invoking the event for that item
     * as well as the {@linkplain #WILDCARD_ITEM_EVENT_V2 wildcard event}.
     */
    public static ItemComponentContainerFactory createItemStackContainerFactory(Item item) {
        Identifier itemId = Registry.ITEM.getId(item);
        Class<? extends ItemComponentContainerFactory> factoryClass = StaticItemComponentPlugin.INSTANCE.getFactoryClass(itemId);
        return ComponentsInternals.createFactory(factoryClass, WILDCARD_ITEM_EVENT_V2, ((ItemCaller) item).cardinal_getItemComponentEventV2());
    }

    public static void copyComponents(ItemStack original, ItemStack copy) {
        ComponentProvider from = ComponentProvider.fromItemStack(original);
        ComponentProvider.fromItemStack(copy).forEachComponent((type, component) -> {
                copyComponent(type, (CopyableComponent<?>) component, from);
            }
        );
    }

    private static <C extends Component> void copyComponent(ComponentType<?> type, CopyableComponent<C> component, ComponentProvider from) {
        @SuppressWarnings("unchecked") C fromComponent = (C) type.getNullable(from);
        if (fromComponent != null) {
            component.copyFrom(fromComponent);
        }
    }

    private static <C extends Component> void copyComponent(CopyableComponent<C> ccp, ComponentProvider from) {
    }

    public static boolean areComponentsIncompatible(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) {
            return stack1.isEmpty() != stack2.isEmpty();
        }
        InternalComponentProvider accessor = (InternalComponentProvider) ComponentProvider.fromItemStack(stack1);
        InternalComponentProvider other = (InternalComponentProvider) ComponentProvider.fromItemStack(stack2);
        ComponentContainer<?> types = accessor.getComponentContainer();
        if (types.size() != other.getComponentContainer().size()) {
            return true;
        }
        for(Map.Entry<ComponentType<?>, ? extends Component> entry : types.entrySet()) {
            @Nullable Component otherComponent = other.getComponent(entry.getKey());
            if(otherComponent == null || !entry.getValue().isComponentEqual(otherComponent)) {
                return true;
            }
        }
        return false;
    }

}
