/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.cca.api.v3.level;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.minecraft.world.WorldProperties;
import org.jetbrains.annotations.ApiStatus;

/**
 * @since 2.4.0
 */
@ApiStatus.Experimental
public interface LevelComponentFactoryRegistry {
    /**
     * Registers a {@link ComponentFactory} for {@link WorldProperties}.
     *
     * @param factory the factory to use to create components of the given type
     */
    <C extends Component> void register(ComponentKey<C> type, ComponentFactory<WorldProperties, ? extends C> factory);

    /**
     * Registers a {@link ComponentFactory} for {@link WorldProperties}.
     *
     * @param factory the factory to use to create components of the given type
     */
    <C extends Component> void register(ComponentKey<? super C> type, Class<C> impl, ComponentFactory<WorldProperties, ? extends C> factory);
}
