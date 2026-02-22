/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2026 Ladysnake
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

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueInput;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.internal.base.AbstractComponentContainer;
import org.ladysnake.cca.internal.base.ComponentsInternals;

public class CcaSaveData extends SavedData {
    public static final ThreadLocal<Boolean> LOADING = ThreadLocal.withInitial(() -> false);
    private static final Identifier PERSISTENT_STATE_KEY = Identifier.fromNamespaceAndPath("cardinal-components", "world");
    public static SavedDataType<CcaSaveData> stateType(ComponentContainer components, HolderLookup.Provider registries) {
        return new SavedDataType<>(
            PERSISTENT_STATE_KEY,
            () -> new CcaSaveData(components),
            CompoundTag.CODEC.xmap(
                nbt -> fromNbt(components, nbt, registries),
                state -> state.writeNbt(new CompoundTag(), registries)
            ),
            DataFixTypes.LEVEL
        );
    }

    private final ComponentContainer components;

    public CcaSaveData(ComponentContainer components) {
        super();
        this.components = components;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public CompoundTag writeNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        var componentsNbt = this.components.toOrphanTag(registryLookup);
        if (componentsNbt != null) {
            tag.put(AbstractComponentContainer.NBT_KEY, componentsNbt);
        }
        return tag;
    }

    public static CcaSaveData fromNbt(ComponentContainer components, CompoundTag tag, HolderLookup.Provider registryLookup) {
        CcaSaveData state = new CcaSaveData(components);
        try (var errorReporter = new ProblemReporter.ScopedCollector(() -> "World", ComponentsInternals.LOGGER)) {
            state.components.readData(TagValueInput.create(errorReporter, registryLookup, tag));
        }
        return state;
    }
}
