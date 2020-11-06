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
package dev.onyxstudios.cca.api.v3.world;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;

/**
 * Applied to a method to declare it as a component factory for {@linkplain World worlds}.
 *
 * <p>When invoked, the factory must return a {@link Component} of the right type.
 *
 * @since 2.4.0
 */
@FunctionalInterface
public interface WorldComponentFactory<C extends Component> {
    /**
     * Initialize components for the given world.
     *
     * <p>The component returned by this method will be available
     * on the world as soon as all component factories have been invoked.
     *
     * @param world the world being constructed
     */
    @Contract(pure = true)
    C createForWorld(World world);
}
