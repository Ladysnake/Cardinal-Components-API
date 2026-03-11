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

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.CopyableComponent;
import org.ladysnake.cca.api.v3.component.TransientComponent;
import org.ladysnake.cca.mixin.base.NbtReadViewAccessor;

import java.util.Iterator;

/**
 * Implementing class for {@link ComponentContainer}.
 */
public abstract class AbstractComponentContainer implements ComponentContainer {

    public static final String NBT_KEY = "cardinal_components";

    @Override
    public void copyFrom(ComponentContainer other, RegistryWrapper.WrapperLookup registryLookup) {
        for (ComponentKey<?> key : this.keys()) {
            Component theirs = key.getInternal(other);
            Component ours = key.getInternal(this);
            assert ours != null;

            if (theirs != null && !ours.equals(theirs)) {
                if (ours instanceof CopyableComponent<?>) {
                    @SuppressWarnings("unchecked") CopyableComponent<Component> copyable = (CopyableComponent<Component>) ours;
                    copyable.copyFrom(theirs, registryLookup);
                } else {
                    try (var errorReporter = new ErrorReporter.Logging(ComponentsInternals.LOGGER)) {
                        NbtWriteView writeView = NbtWriteView.create(errorReporter, registryLookup);
                        theirs.writeData(writeView);
                        ours.readData(NbtReadView.create(errorReporter, registryLookup, writeView.getNbt()));
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
    public void readData(ReadView readView) {
        readOrphanData(readView.getReadView(NBT_KEY));
    }

    @Override
    public void readOrphanData(ReadView componentMap) {
        NbtCompound underlyingNbt = componentMap instanceof NbtReadViewAccessor nbtReadView ? nbtReadView.getNbt() : null;
        for (ComponentKey<?> key : this.keys()) {
            String keyId = key.getId().toString();

            ReadView componentData = componentMap.getReadView(keyId);
            Component component = key.getInternal(this);
            assert component != null;
            component.readData(componentData);
            if (underlyingNbt != null) {
                underlyingNbt.remove(keyId);
            }
        }

        if (underlyingNbt != null) {
            ComponentsInternals.logDeserializationWarnings(underlyingNbt.getKeys());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation first checks if the container is empty; if so it
     * returns immediately. Then, it iterates over this container's mappings, and creates
     * a compound tag for each component. The tag is then passed to the component's
     * {@link Component#writeData(WriteView)} method. Every such serialized component is appended
     * to a {@code NbtCompound}, using the component type's identifier as the key.
     * The serialized map is finally appended to the passed in tag using the "cardinal_components" key.
     */
    @Override
    public void writeData(WriteView writeView) {
        if(this.hasComponents()) {
            writeOrphanData(writeView.get(NBT_KEY));
        }
    }

    @Override
    public void writeOrphanData(WriteView writeView) {
        for (ComponentKey<?> type : this.keys()) {
            Component component = type.getFromContainer(this);
            if (!(component instanceof TransientComponent)) {
                component.writeData(writeView.get(type.getId().toString()));
            }
        }
    }

    @Override
    public @Nullable NbtCompound toOrphanTag(RegistryWrapper.WrapperLookup registryLookup) {
        try (var errorReporter = new ErrorReporter.Logging(ComponentsInternals.LOGGER)) {
            NbtWriteView writeView = NbtWriteView.create(errorReporter, registryLookup);
            writeOrphanData(writeView);
            return writeView.isEmpty() ? null : writeView.getNbt();
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
            Component value = key.getInternal(this);
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
