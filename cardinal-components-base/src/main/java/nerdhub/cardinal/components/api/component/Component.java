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

import nerdhub.cardinal.components.api.util.NbtSerializable;
import net.minecraft.nbt.CompoundTag;

/**
 * base class for components.<br/>
 * provides basic serialization capability.<br/>
 * <p>Item Components <strong>MUST</strong> override and implement all methods of this interface!</p>
 */
public interface Component extends NbtSerializable {

    /**
     * Reads this component's properties from a {@link CompoundTag}.
     *
     * @param tag a {@code CompoundTag} on which this component's serializable data has been written
     * @implNote implementations must not assert that the data written on the tag corresponds to any
     * specific scheme, as saved data is susceptible to external tempering, and may come from an earlier
     * version.
     */
    @Override
    void fromTag(CompoundTag tag);

    /**
     * Writes this component's properties to a {@link CompoundTag}.
     *
     * @param tag a {@code CompoundTag} on which to write this component's serializable data
     * @return {@code tag}
     * @implNote this method must <strong>NOT</strong> write any value associated with the {@code "componentId"} key
     * in the given tag.
     */
    @Override
    CompoundTag toTag(CompoundTag tag);

    /**
     * Used to compare two instances of components for equality.
     */
    // TODO deprecate in favor of Object#equals
    default boolean isComponentEqual(Component other) {
        return this.equals(other);
    }
}
