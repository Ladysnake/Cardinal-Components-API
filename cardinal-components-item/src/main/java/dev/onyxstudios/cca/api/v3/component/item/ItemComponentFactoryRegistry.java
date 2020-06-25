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
package dev.onyxstudios.cca.api.v3.component.item;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * @since 2.4.0
 */
@ApiStatus.Experimental
public interface ItemComponentFactoryRegistry {
    /**
     * Registers an {@link ItemComponentFactory} for stacks of a specific item.
     *
     * @param itemId  the id of an item to target
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    <C extends CopyableComponent<?>> void registerFor(Identifier itemId, ComponentKey<? super C> type, ItemComponentFactory<C> factory);

    /**
     * Registers an {@link ItemComponentFactoryV2} for stacks of a specific item.
     *
     * @param itemId  the id of an item to target
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    <C extends CopyableComponent<?>> void registerFor(Identifier itemId, ComponentKey<? super C> type, ItemComponentFactoryV2<C> factory);

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
     */
    <C extends CopyableComponent<?>> void registerForAll(ComponentKey<? super C> type, ItemComponentFactory<C> factory);

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
     */
    <C extends CopyableComponent<?>> void registerForAll(ComponentKey<? super C> type, ItemComponentFactoryV2<C> factory);
}
