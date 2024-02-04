/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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
package org.ladysnake.cca.api.v3.item;

import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.TransientComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * Note: Item components are experimental and may be phased out entirely once an item implementation
 * of the Fabric API Lookup API exists.
 *
 * @since 2.4.0
 */
public interface ItemComponentFactoryRegistry {
    /**
     * Registers a {@link ComponentFactory} for stacks of specific items, based on a predicate.
     *
     * @param test    a predicate testing whether the Item can have the component attached to its stacks
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     * @since 2.7.10
     */
    @ApiStatus.Experimental
    <C extends ItemComponent> void register(Predicate<Item> test, ComponentKey<? super C> type, ComponentFactory<ItemStack, C> factory);

    /**
     * Registers a {@link ComponentFactory} for stacks of a specific item.
     *
     * @param item    the item to target
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException  if any of the arguments is {@code null}
     * @throws IllegalStateException if the {@code item} was not previously registered
     * @since 2.7.10
     */
    @ApiStatus.Experimental
    <C extends ItemComponent> void register(Item item, ComponentKey<? super C> type, ComponentFactory<ItemStack, C> factory);

    /**
     * Registers a {@link ComponentFactory} for stacks of specific items, based on a predicate.
     *
     * @param test    a predicate testing whether the Item can have the component attached to its stacks
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     * @since 2.7.10
     * @see TransientComponent.SimpleImpl
     */
    @ApiStatus.Experimental
    <C extends TransientComponent> void registerTransient(Predicate<Item> test, ComponentKey<? super C> type, ComponentFactory<ItemStack, C> factory);

    /**
     * Registers a {@link ComponentFactory} for stacks of a specific item.
     *
     * @param item    the item to target
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException  if any of the arguments is {@code null}
     * @throws IllegalStateException if the {@code item} was not previously registered
     * @since 2.7.10
     * @see TransientComponent.SimpleImpl
     */
    @ApiStatus.Experimental
    <C extends TransientComponent> void registerTransient(Item item, ComponentKey<? super C> type, ComponentFactory<ItemStack, C> factory);
}
