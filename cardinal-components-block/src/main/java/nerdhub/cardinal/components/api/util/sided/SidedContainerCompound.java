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
package nerdhub.cardinal.components.api.util.sided;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import nerdhub.cardinal.components.api.util.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A side-aware compound component container.
 * Each direction of a cubic space is associated with a separate component container.
 * <p> In this context, the {@code null} side acts as a default value.
 * Attaching a component to the {@code null} side generally
 * implies that it should be accessible from any side. A query for a component on
 * a {@code null} side should only be made when the caller does not have specific
 * side information, and will return only elements attached to the {@code null} side.
 */
public interface SidedContainerCompound extends NbtSerializable {
    ComponentContainer get(@Nullable Direction side);

    void fromTag(CompoundTag serialized);

    CompoundTag toTag(CompoundTag tag);
}
