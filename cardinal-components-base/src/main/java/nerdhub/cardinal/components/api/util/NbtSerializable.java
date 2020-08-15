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
package nerdhub.cardinal.components.api.util;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.ApiStatus;

public interface NbtSerializable {
    /**
     * Reads this object's properties from a {@link CompoundTag}.
     *
     * @param tag a {@code CompoundTag} on which this object's serializable data has been written
     * @implNote implementations must not assert that the data written on the tag corresponds to any
     * specific scheme, as saved data is susceptible to external tempering, and may come from an earlier
     * version. They should also store values into {@code tag} using only unique namespaced keys, as other
     * information may be stored in said tag.
     */
    void fromTag(CompoundTag tag);

    /**
     * Writes this object's properties to a {@link CompoundTag}.
     *
     * @param tag a {@code CompoundTag} on which to write this component's serializable data
     * @return {@code tag} for easy chaining
     */
    CompoundTag toTag(CompoundTag tag);

    @ApiStatus.Experimental
    default void fromDynamic(Dynamic<?> dynamic) {
        this.fromTag((CompoundTag) dynamic.convert(NbtOps.INSTANCE).getValue());
    }

    @ApiStatus.Experimental
    default <T> Dynamic<T> toDynamic(Dynamic<T> dynamic) {
        return dynamic.convert(NbtOps.INSTANCE).map(tag -> this.toTag((CompoundTag)tag)).convert(dynamic.getOps());
    }
}
