/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 Ladysnake
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
package org.ladysnake.cca.api.v3.world;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;

/**
 * @since 2.4.0
 */
public interface WorldComponentFactoryRegistry {
    /**
     * Registers a {@link ComponentFactory} for all {@linkplain World worlds}.
     *
     * <p>If the component's actual implementation has different capabilities to {@code C}
     * (typically if it is ticking and {@code C} is not), one should use the {@linkplain #register(ComponentKey, Class, ComponentFactory) dedicated overload}.
     *
     * @param factory the factory to use to create components of the given type
     */
    <C extends Component> void register(ComponentKey<C> type, ComponentFactory<World, ? extends C> factory);

    /**
     * Registers a {@link ComponentFactory} for all {@link World worlds}, specifying which implementation of the component interface is used.
     *
     * @param factory the factory to use to create components of the given type
     */
    <C extends Component> void register(ComponentKey<? super C> type, Class<C> impl, ComponentFactory<World, ? extends C> factory);

    /**
     * Registers a {@link ComponentFactory} only for {@linkplain World worlds} with the given {@code dimensionId}.
     *
     * <p>If the component's actual implementation has different capabilities to {@code C}
     * (typically if it is ticking and {@code C} is not), one should use the {@linkplain #registerFor(RegistryKey, ComponentKey, Class, ComponentFactory) dedicated overload}.
     *
     * @param factory the factory to use to create components of the given type
     * @since 6.0.0
     */
    <C extends Component> void registerFor(RegistryKey<World> dimensionId, ComponentKey<C> type, ComponentFactory<World, ? extends C> factory);

    /**
     * Registers a {@link ComponentFactory} only for {@linkplain World worlds} with the given {@code dimensionId},
     * specifying which implementation of the component interface is used.
     *
     * @param factory the factory to use to create components of the given type
     * @since 6.0.0
     */
    <C extends Component> void registerFor(RegistryKey<World> dimensionId, ComponentKey<? super C> type, Class<C> impl, ComponentFactory<World, ? extends C> factory);

}
