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
package org.ladysnake.cca.internal.base;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.*;

import java.util.Iterator;
import java.util.Optional;

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
                    NbtCompound tag = new NbtCompound();
                    theirs.writeToNbt(tag, registryLookup);
                    ours.readFromNbt(tag, registryLookup);
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
    public void fromTag(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        Optional<NbtList> list = tag.getList(NBT_KEY);
        if(list.isPresent()) {
            NbtList componentList = list.get();
            for (int i = 0; i < componentList.size(); i++) {
                NbtCompound nbt = componentList.getCompoundOrEmpty(i);
                Optional<ComponentKey<?>> type = nbt.getString("componentId").map(Identifier::tryParse).map(ComponentRegistry::get);
                if (type.isPresent()) {
                    Component component = type.get().getInternal(this);
                    if (component != null) {
                        component.readFromNbt(nbt, registryLookup);
                    }
                }
            }
        } else {
            Optional<NbtCompound> compound = tag.getCompound(NBT_KEY);

            if (compound.isPresent()) {
                NbtCompound componentMap = compound.get();
                fromOrphanTag(componentMap, registryLookup);
            }
        }
    }

    @Override
    public void fromOrphanTag(NbtCompound componentMap, RegistryWrapper.WrapperLookup registryLookup) {
        for (ComponentKey<?> key : this.keys()) {
            String keyId = key.getId().toString();

            Optional<NbtCompound> componentNbt = componentMap.getCompound(keyId);
            if (componentNbt.isPresent()) {
                Component component = key.getInternal(this);
                assert component != null;
                component.readFromNbt(componentNbt.get(), registryLookup);
                componentMap.remove(keyId);
            }
        }

        ComponentsInternals.logDeserializationWarnings(componentMap.getKeys());
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation first checks if the container is empty; if so it
     * returns immediately. Then, it iterates over this container's mappings, and creates
     * a compound tag for each component. The tag is then passed to the component's
     * {@link Component#writeToNbt(NbtCompound, RegistryWrapper.WrapperLookup)} method. Every such serialized component is appended
     * to a {@code NbtCompound}, using the component type's identifier as the key.
     * The serialized map is finally appended to the passed in tag using the "cardinal_components" key.
     */
    @Override
    public NbtCompound toTag(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if(this.hasComponents()) {
            NbtCompound componentMap = toOrphanTag(registryLookup);
            if (componentMap != null) {
                tag.put(NBT_KEY, componentMap);
            }
        }
        return tag;
    }

    @Override
    public @Nullable NbtCompound toOrphanTag(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound componentMap = null;
        NbtCompound componentTag = new NbtCompound();

        for (ComponentKey<?> type : this.keys()) {
            Component component = type.getFromContainer(this);
            component.writeToNbt(componentTag, registryLookup);

            if (!componentTag.isEmpty()) {
                if (componentMap == null) {
                    componentMap = new NbtCompound();
                }

                componentMap.put(type.getId().toString(), componentTag);
                componentTag = new NbtCompound();   // recycle tag objects if possible
            }
        }
        return componentMap;
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
