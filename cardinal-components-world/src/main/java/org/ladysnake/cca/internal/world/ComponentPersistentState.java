/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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
package org.ladysnake.cca.internal.world;

import org.ladysnake.cca.api.v3.component.ComponentContainer;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public class ComponentPersistentState extends PersistentState {
    public static final ThreadLocal<Boolean> LOADING = ThreadLocal.withInitial(() -> false);

    public static Type<ComponentPersistentState> getType(ComponentContainer components) {
        return new Type<>(
            () -> new ComponentPersistentState(components),
            tag -> ComponentPersistentState.fromNbt(components, tag),
            DataFixTypes.LEVEL
        );
    }

    private final ComponentContainer components;

    public ComponentPersistentState(ComponentContainer components) {
        super();
        this.components = components;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        return this.components.toTag(tag);
    }

    public static ComponentPersistentState fromNbt(ComponentContainer components, NbtCompound tag) {
        ComponentPersistentState state = new ComponentPersistentState(components);
        state.components.fromTag(tag);
        return state;
    }
}
