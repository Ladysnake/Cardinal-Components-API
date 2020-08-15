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

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import nerdhub.cardinal.components.api.component.Component;
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
import java.util.Set;

public final class CardinalItemInternals {
    public static final Event<ItemComponentCallbackV2> WILDCARD_ITEM_EVENT_V2 = createItemComponentsEventV2();
    public static final Event<ItemComponentCallback> WILDCARD_ITEM_EVENT = createItemComponentsEvent(WILDCARD_ITEM_EVENT_V2);
    public static final String CCA_SYNCED_COMPONENTS = "cca_synced_components";

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
        Class<? extends ItemComponentContainerFactory> factoryClass = StaticItemComponentPlugin.INSTANCE.getFactoryClass(item, itemId);
        return ComponentsInternals.createFactory(factoryClass, WILDCARD_ITEM_EVENT_V2, ((ItemCaller) item).cardinal_getItemComponentEventV2());
    }

    public static void copyComponents(ItemStack original, ItemStack copy) {
        ComponentContainer<?> originalComponents = InternalStackComponentProvider.get(original).getActualComponentContainer();
        ComponentContainer<?> copiedComponents = InternalStackComponentProvider.get(copy).getActualComponentContainer();

        for (ComponentKey<?> key : copiedComponents.keys()) {
            copyComponent(key, (CopyableComponent<?>) key.getFromContainer(copiedComponents), originalComponents);
        }
    }

    private static <C extends Component> void copyComponent(ComponentKey<?> type, CopyableComponent<C> component, ComponentContainer<?> from) {
        @SuppressWarnings("unchecked") C fromComponent = (C) type.getInternal(from);

        if (fromComponent != null) {
            component.copyFrom(fromComponent);
        }
    }

    public static boolean areComponentsIncompatible(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) {
            return stack1.isEmpty() != stack2.isEmpty();
        }

        Set<ComponentKey<?>> keys1 = ((InternalStackComponentProvider) ComponentProvider.fromItemStack(stack1)).getComponentContainer().keys();
        Set<ComponentKey<?>> keys2 = ((InternalStackComponentProvider) ComponentProvider.fromItemStack(stack2)).getComponentContainer().keys();

        if (keys1.size() != keys2.size()) {
            return true;
        }

        for(ComponentKey<?> key : keys1) {
            @Nullable Component otherComponent = key.getNullable(stack2);
            if(otherComponent == null || !key.get(stack1).isComponentEqual(otherComponent)) {
                return true;
            }
        }

        return false;
    }
}
