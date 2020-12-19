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
package dev.onyxstudios.cca.api.v3.item;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * @since 2.4.0
 */
public interface ItemComponentFactoryRegistry {
    /**
     * Registers an {@link ItemComponentFactory} for stacks of a specific item.
     *
     * @param item  the item to target
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws IllegalStateException if the {@code item} was not previously registered
     */
    <C extends Component> void registerFor(Item item, ComponentKey<C> type, ItemComponentFactory<? extends C> factory);

    /**
     * Registers an {@link ItemComponentFactory} for stacks of a specific item.
     *
     * @param itemId  the id of an item to target
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    <C extends Component> void registerFor(Identifier itemId, ComponentKey<C> type, ItemComponentFactory<? extends C> factory);

    /**
     * Registers an {@link ItemComponentFactoryV2} for stacks of specific items, based on a predicate.
     *
     * @param test  a predicate testing whether the Item can have the component attached to its stacks
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    @ApiStatus.Experimental
    <C extends ItemComponent> void registerForV3(Predicate<Item> test, ComponentKey<? super C> type, ItemComponentFactory<C> factory);

    /**
     * Registers an {@link ItemComponentFactory} for stacks of a specific item.
     *
     * @param item  the item to target
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws IllegalStateException if the {@code item} was not previously registered
     */
    @ApiStatus.Experimental
    <C extends ItemComponent> void registerForV3(Item item, ComponentKey<? super C> type, ItemComponentFactory<C> factory);

    /**
     * Registers an {@link ItemComponentFactoryV2} for stacks of specific items, based on a predicate.
     *
     * @param test  a predicate testing whether the Item can have the component attached to its stacks
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    <C extends Component> void registerFor(Predicate<Item> test, ComponentKey<C> type, ItemComponentFactory<? extends C> factory);

    /**
     * Registers an {@link ItemComponentFactoryV2} for stacks of a specific item.
     *
     * @param itemId  the id of an item to target
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     * @deprecated use {@link #registerFor(Identifier, ComponentKey, ItemComponentFactory)}, as {@link ItemStack#getItem()} is guaranteed to be correct
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    <C extends Component> void registerFor(Identifier itemId, ComponentKey<C> type, ItemComponentFactoryV2<? extends C> factory);

    /**
     * Registers an {@link ItemComponentFactoryV2} for stacks of specific items, based on a predicate.
     *
     * @param test  a predicate testing whether the Item can have the component attached to its stacks
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     * @deprecated use {@link #registerFor(Identifier, ComponentKey, ItemComponentFactory)}, as {@link ItemStack#getItem()} is guaranteed to be correct
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    <C extends Component> void registerFor(Predicate<Item> test, ComponentKey<C> type, ItemComponentFactoryV2<? extends C> factory);

    /**
     * Registers an {@link ItemComponentFactory} for every item.
     *
     * <p> A callback registered using this method is called for every item stack ever
     * created. For performance reasons, wildcard callbacks should be avoided
     * where possible. Notably, when registering callbacks for various items,
     * it is often better to register a separate specialized callback for each one
     * than a single generic callback with additional checks.
     *
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     * @deprecated if you are sure of what you are doing, and you really want to attach a
     * component to every item, use {@code registerFor(i -> true, type, factory)}
     */
    @Deprecated
    <C extends Component> void registerForAll(ComponentKey<C> type, ItemComponentFactory<? extends C> factory);

    /**
     * Registers an {@link ItemComponentFactoryV2} for every item.
     *
     * <p> A callback registered using this method is called for every item stack ever
     * created. For performance reasons, wildcard callbacks should be avoided
     * where possible. Notably, when registering callbacks for various items,
     * it is often better to register a separate specialized callback for each one
     * than a single generic callback with additional checks.
     *
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     * @deprecated if you are sure of what you are doing, and you really want to attach a
     * component to every item, use {@code registerFor(i -> true, type, factory)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    <C extends Component> void registerForAll(ComponentKey<C> type, ItemComponentFactoryV2<? extends C> factory);
}
