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
package dev.onyxstudios.cca.api.v3.block;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;

/**
 * A helper interface for components that entirely rely on contextual information like {@link BlockState}s.
 * @deprecated use {@link net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup} instead
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
public interface BlockComponent extends Component {
    @Override
    default void readFromNbt(CompoundTag tag) {
        // NO-OP, block components must be serialized separately
    }

    @Override
    default void writeToNbt(CompoundTag tag) {
        // NO-OP, block components must be serialized separately
    }
}
