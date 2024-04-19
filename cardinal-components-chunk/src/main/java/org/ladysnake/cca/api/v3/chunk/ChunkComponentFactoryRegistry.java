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
package org.ladysnake.cca.api.v3.chunk;

import net.minecraft.world.chunk.Chunk;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;

/**
 * @since 2.4.0
 */
public interface ChunkComponentFactoryRegistry {
    /**
     * Registers a {@link ComponentFactory} for {@link Chunk}s.
     *
     * @param factory the factory to use to create components of the given type
     */
    <C extends Component> void register(ComponentKey<C> key, ComponentFactory<Chunk, ? extends C> factory);

    /**
     * Registers a {@link ComponentFactory} for {@link Chunk}s.
     *
     * @param impl    the class object representing the type of component produced by the factory
     * @param factory the factory to use to create components of the given type
     */
    <C extends Component> void register(ComponentKey<? super C> key, Class<C> impl, ComponentFactory<Chunk, ? extends C> factory);
}
