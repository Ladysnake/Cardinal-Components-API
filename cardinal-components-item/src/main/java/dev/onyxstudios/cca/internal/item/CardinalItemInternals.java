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
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.InternalComponentProvider;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class CardinalItemInternals {
    public static final Event<ItemComponentCallback> WILDCARD_ITEM_EVENT = createItemComponentsEvent();
    public static final String CCA_SYNCED_COMPONENTS = "cca_synced_components";

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
    public static DynamicContainerFactory<ItemStack> createItemStackContainerFactory(Item item) {
        Identifier itemId = Registry.ITEM.getId(item);
        Class<? extends DynamicContainerFactory<ItemStack>> factoryClass = StaticItemComponentPlugin.INSTANCE.getFactoryClass(item, itemId);
        return ComponentsInternals.createFactory(factoryClass, WILDCARD_ITEM_EVENT, ((ItemCaller) item).cardinal_getItemComponentEvent());
    }

    public static void copyComponents(ItemStack original, ItemStack copy) {
        InternalStackComponentProvider originalProvider = InternalStackComponentProvider.get(original);
        InternalStackComponentProvider copiedProvider = InternalStackComponentProvider.get(copy);
        ComponentContainer originalComponents = originalProvider.getActualComponentContainer();
        ComponentContainer copiedComponents = copiedProvider.getActualComponentContainer();
        CompoundTag serializedComponents;

        if (originalComponents != null) {
            // the original stack has live components
            if (copiedComponents != null) {
                // both stacks' components are initialized
                copiedComponents.copyFrom(originalComponents);
            } else if (originalComponents.hasComponents()) {
                // only the original stack's components are initialized
                CompoundTag tag = new CompoundTag();
                originalComponents.toTag(tag);
                copiedProvider.cca_setSerializedComponentData(tag);
            }
        } else if ((serializedComponents = originalProvider.cca_getSerializedComponentData()) != null) {
            // the original stack has frozen components
            if (copiedComponents != null) {
                // only the copied stack's components are initialized (unlikely)
                copiedComponents.fromTag(serializedComponents);
            } else {
                // no components are initialized
                copiedProvider.cca_setSerializedComponentData(serializedComponents.copy());
            }
        }
    }

    public static boolean areComponentsIncompatible(ItemStack stack1, ItemStack stack2) {
        // this method should only be called to supplement an equals check, not to check if 2
        // unrelated stacks have compatible components
        if (stack1.getItem() != stack2.getItem()) {
            return true;
        }

        if (stack1.isEmpty()) return false;

        // Possibly initialize components
        Set<ComponentKey<?>> keys1 = ((InternalComponentProvider) ComponentProvider.fromItemStack(stack1)).getComponentContainer().keys();

        for(ComponentKey<?> key : keys1) {
            @Nullable Component otherComponent = key.getNullable(stack2);
            // TODO replace with Objects.equals(key.getNullable(stack1), key.getNullable(stack2))
            if(otherComponent == null || !key.get(stack1).isComponentEqual(otherComponent)) {
                return true;
            }
        }

        return false;
    }
}
