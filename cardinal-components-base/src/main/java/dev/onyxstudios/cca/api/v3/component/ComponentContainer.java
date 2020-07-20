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
package dev.onyxstudios.cca.api.v3.component;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.util.NbtSerializable;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

/**
 * A container for components.
 *
 * <p> Component values obey 2 constraints.
 * <ul>
 *     <li>Every component in a {@code ComponentContainer<C>} is an instance of {@code C}</li>
 *     <li>A component mapped to a {@code ComponentType<T>} is also an instance of {@code T}</li>
 * </ul>
 * Both type constraints should generally be interfaces, to allow multiple inheritance.<br><br>
 *
 * <p> A {@code ComponentContainer} cannot have its components removed.
 * Components can be added or replaced, but removal of existing component types
 * is unsupported. This guarantees consistent behaviour for consumers.
 *
 * @param <C> The upper bound for components stored in this container
 */
@ApiStatus.NonExtendable
@ApiStatus.Experimental
public interface ComponentContainer<C extends Component> extends NbtSerializable {
    Set<ComponentKey<?>> keys();

    Class<C> getComponentClass();
}
