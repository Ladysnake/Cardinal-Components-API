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
package org.ladysnake.cca.internal.base;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.CopyableComponent;
import org.ladysnake.cca.api.v3.component.TransientComponent;
import org.ladysnake.cca.api.v8.component.CardinalComponent;
import org.ladysnake.cca.mixin.base.TagValueInputAccessor;

import java.util.Iterator;

/**
 * Implementing class for {@link ComponentContainer}.
 */
public abstract class AbstractComponentContainer implements ComponentContainer {

    public static final String NBT_KEY = "cardinal_components";

    @Override
    public void copyFrom(ComponentContainer other, HolderLookup.Provider registryLookup) {
        for (ComponentKey<?> key : this.keys()) {
            CardinalComponent theirs = key.getInternal(other);
            CardinalComponent ours = key.getInternal(this);
            assert ours != null;

            if (theirs != null && !ours.equals(theirs)) {
                if (ours instanceof CopyableComponent<?>) {
                    @SuppressWarnings("unchecked") CopyableComponent<CardinalComponent> copyable = (CopyableComponent<CardinalComponent>) ours;
                    copyable.copyFrom(theirs, registryLookup);
                } else {
                    try (var errorReporter = new ProblemReporter.ScopedCollector(ComponentsInternals.LOGGER)) {
                        TagValueOutput writeView = TagValueOutput.createWithContext(errorReporter, registryLookup);
                        theirs.writeData(writeView);
                        ours.readData(TagValueInput.create(errorReporter, registryLookup, writeView.buildResult()));
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation first checks if {@code tag} has a tag list
     * mapped to the "cardinal_components" key; if not it returns immediately.
     * Then it iterates over the list's tags, casts them to {@code NbtCompound},
     * and passes them to the associated component's {@code fromTag} method.
     * If this container lacks a corresponding component for a serialized component
     * type, the component tag is skipped.
     */
    @Override
    public void readData(ValueInput readView) {
        readOrphanData(readView.childOrEmpty(NBT_KEY));
    }

    @Override
    public void readOrphanData(ValueInput componentMap) {
        CompoundTag underlyingNbt = componentMap instanceof TagValueInputAccessor nbtReadView ? nbtReadView.getInput() : null;
        for (ComponentKey<?> key : this.keys()) {
            String keyId = key.getId().toString();

            ValueInput componentData = componentMap.childOrEmpty(keyId);
            CardinalComponent component = key.getInternal(this);
            assert component != null;
            component.readData(componentData);
            if (underlyingNbt != null) {
                underlyingNbt.remove(keyId);
            }
        }

        if (underlyingNbt != null) {
            ComponentsInternals.logDeserializationWarnings(underlyingNbt.keySet());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation first checks if the container is empty; if so it
     * returns immediately. Then, it iterates over this container's mappings, and creates
     * a compound tag for each component. The tag is then passed to the component's
     * {@link CardinalComponent#writeData(ValueOutput)} method. Every such serialized component is appended
     * to a {@code NbtCompound}, using the component type's identifier as the key.
     * The serialized map is finally appended to the passed in tag using the "cardinal_components" key.
     */
    @Override
    public void writeData(ValueOutput writeView) {
        if(this.hasComponents()) {
            writeOrphanData(writeView.child(NBT_KEY));
        }
    }

    @Override
    public void writeOrphanData(ValueOutput writeView) {
        for (ComponentKey<?> type : this.keys()) {
            CardinalComponent component = type.getFromContainer(this);
            if (!(component instanceof TransientComponent)) {
                component.writeData(writeView.child(type.getId().toString()));
            }
        }
    }

    @Override
    public @Nullable CompoundTag toOrphanTag(HolderLookup.Provider registryLookup) {
        try (var errorReporter = new ProblemReporter.ScopedCollector(ComponentsInternals.LOGGER)) {
            TagValueOutput writeView = TagValueOutput.createWithContext(errorReporter, registryLookup);
            writeOrphanData(writeView);
            return writeView.isEmpty() ? null : writeView.buildResult();
        }
    }

    @Override
    public String toString() {
        Iterator<ComponentKey<?>> i = this.keys().iterator();

        if (! i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            ComponentKey<?> key = i.next();
            CardinalComponent value = key.getInternal(this);
            sb.append(key);
            sb.append('=');
            sb.append(value);

            if (! i.hasNext()) {
                return sb.append('}').toString();
            }

            sb.append(',').append(' ');
        }
    }
}
