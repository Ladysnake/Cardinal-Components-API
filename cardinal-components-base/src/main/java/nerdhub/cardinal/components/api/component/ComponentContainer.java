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
package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.NbtSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

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
public interface ComponentContainer<C extends Component> extends Map<ComponentType<?>, C>, NbtSerializable {

    /**
     * Returns <tt>true</tt> if this container contains a component associated with
     * the specified key. (There can be at most one such mapping.)
     *
     * @param key key whose presence in this container is to be tested
     * @return <tt>true</tt> if this container contains a mapping for the specified
     * key
     * @throws NullPointerException if the specified key is null
     */
    boolean containsKey(ComponentType<?> key);

    /**
     * Returns the value to which the specified component type is mapped,
     * or {@code null} if this container contains no component of that type.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key == k)},
     * then this method returns {@code v}; otherwise it returns
     * {@code null}.  (There can be at most one such mapping.)
     *
     * @param key a registered component type
     * @param <T> the class of the component
     * @return a component of that type, of {@code null} if none has been attached
     * @throws NullPointerException if the specified key is null
     */
    @Nullable
    <T extends Component> T get(ComponentType<T> key);

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with specified key, or
     * <tt>null</tt> if there was no mapping for key.
     * @throws NullPointerException          if the specified key or value is null
     * @throws UnsupportedOperationException if the <tt>put</tt> operation
     *                                       is not supported by this container
     * @throws IllegalStateException         if this container already contains a mapping for the given key
     * @throws IllegalArgumentException      if {@code value} is not a valid instance for {@code key}
     * @implSpec Implementations that do not support modification should
     * document their immutability properties
     */
    @Nullable
    @Override
    C put(@Nonnull ComponentType<?> key, @Nonnull C value);

    @Deprecated
    @Override default C remove(Object key) {
        throw new UnsupportedOperationException();
    }
}
