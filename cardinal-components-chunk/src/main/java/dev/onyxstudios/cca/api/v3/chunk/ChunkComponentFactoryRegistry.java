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
package dev.onyxstudios.cca.api.v3.chunk;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import nerdhub.cardinal.components.api.component.Component;
import org.jetbrains.annotations.ApiStatus;

/**
 * @since 2.4.0
 */
public interface ChunkComponentFactoryRegistry {
    /**
     * Registers a {@link ChunkComponentFactory}.
     *
     * @param factory the factory to use to create components of the given type
     */
    <C extends Component> void register(ComponentKey<C> type, ChunkComponentFactory<? extends C> factory);

    /**
     * Begin a factory registration.
     *
     * @param key    the key of components to attach
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    @ApiStatus.Experimental
    <C extends Component> Registration<C> beginRegistration(ComponentKey<C> key);

    @ApiStatus.Experimental
    interface Registration<C extends Component> {
        /**
         * Specify the implementation class that will be produced by the factory.
         *
         * <p>Properties of the component are detected using the available class information.
         * If the implementation is not specified, {@link ComponentKey#getComponentClass()}
         * will be used.
         */
        <I extends C> Registration<I> impl(Class<I> impl);

        /**
         * Complete the ongoing registration.
         *
         * @param factory a factory creating instances of {@code C} that will be attached to instances of {@code E}
         */
        void end(ChunkComponentFactory<C> factory);
    }
}
