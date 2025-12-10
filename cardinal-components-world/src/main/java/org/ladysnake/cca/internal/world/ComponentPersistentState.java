/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2025 Ladysnake
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

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.internal.base.AbstractComponentContainer;
import org.ladysnake.cca.internal.base.ComponentsInternals;

public class ComponentPersistentState extends PersistentState {
    public static final ThreadLocal<Boolean> LOADING = ThreadLocal.withInitial(() -> false);
    private static final String PERSISTENT_STATE_KEY = "cardinal_world_components";
    public static PersistentStateType<ComponentPersistentState> stateType(ComponentContainer components, RegistryWrapper.WrapperLookup registries) {
        return new PersistentStateType<>(
            PERSISTENT_STATE_KEY,
            () -> new ComponentPersistentState(components),
            NbtCompound.CODEC.xmap(
                nbt -> fromNbt(components, nbt, registries),
                state -> state.writeNbt(new NbtCompound(), registries)
            ),
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

    public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        var componentsNbt = this.components.toOrphanTag(registryLookup);
        if (componentsNbt != null) {
            tag.put(AbstractComponentContainer.NBT_KEY, componentsNbt);
        }
        return tag;
    }

    public static ComponentPersistentState fromNbt(ComponentContainer components, NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        ComponentPersistentState state = new ComponentPersistentState(components);
        try (var errorReporter = new ErrorReporter.Logging(() -> "World", ComponentsInternals.LOGGER)) {
            state.components.readData(NbtReadView.create(errorReporter, registryLookup, tag));
        }
        return state;
    }
}
