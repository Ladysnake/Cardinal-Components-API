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
package dev.onyxstudios.cca.mixin.item.common;

import dev.onyxstudios.cca.internal.item.CardinalItemInternals;
import dev.onyxstudios.cca.internal.item.ItemCaller;
import dev.onyxstudios.cca.internal.item.ItemComponentContainerFactory;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import nerdhub.cardinal.components.api.event.ItemComponentCallbackV2;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public abstract class MixinItem implements ItemCaller {
    @Unique private final Event<ItemComponentCallbackV2> cardinal_componentsEventV2 = CardinalItemInternals.createItemComponentsEventV2();
    @Unique private final Event<ItemComponentCallback> cardinal_componentsEvent = CardinalItemInternals.createItemComponentsEvent(this.cardinal_componentsEventV2);
    @Unique private ItemComponentContainerFactory cardinal_containerFactory;

    @Override
    public Event<ItemComponentCallback> cardinal_getItemComponentEvent() {
        return this.cardinal_componentsEvent;
    }

    @Override
    public Event<ItemComponentCallbackV2> cardinal_getItemComponentEventV2() {
        return this.cardinal_componentsEventV2;
    }

    @Override
    public ComponentContainer<CopyableComponent<?>> cardinal_createComponents(ItemStack stack) {
        // assert stack.getItem() == this;
        if (this.cardinal_containerFactory == null) {
            this.cardinal_containerFactory = CardinalItemInternals.createItemStackContainerFactory((Item) (Object) this);
        }
        return this.cardinal_containerFactory.create((Item) (Object) this, stack);
    }
}
