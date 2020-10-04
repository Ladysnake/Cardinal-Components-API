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
package dev.onyxstudios.cca.internal.base;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.util.Iterator;

/**
 * Implementing class for {@link ComponentContainer}.
 */
public abstract class AbstractComponentContainer implements ComponentContainer {

    public static final String NBT_KEY = "cardinal_components";

    @Override
    public void copyFrom(ComponentContainer other) {
        for (ComponentKey<?> key : this.keys()) {
            Component theirs = key.getInternal(other);
            Component ours = key.getInternal(this);
            assert ours != null;

            if (theirs != null && !ours.equals(theirs)) {
                if (ours instanceof CopyableComponent) {
                    @SuppressWarnings("unchecked") CopyableComponent<Component> copyable = (CopyableComponent<Component>) ours;
                    copyable.copyFrom(theirs);
                } else {
                    CompoundTag tag = new CompoundTag();
                    theirs.writeToNbt(tag);
                    ours.readFromNbt(tag);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation first checks if {@code tag} has a tag list
     * mapped to the "cardinal_components" key; if not it returns immediately.
     * Then it iterates over the list's tags, casts them to {@code CompoundTag},
     * and passes them to the associated component's {@code fromTag} method.
     * If this container lacks a corresponding component for a serialized component
     * type, the component tag is skipped.
     */
    @Override
    public void fromTag(CompoundTag tag) {
        if(tag.contains(NBT_KEY, NbtType.LIST)) {
            ListTag componentList = tag.getList(NBT_KEY, NbtType.COMPOUND);
            for (int i = 0; i < componentList.size(); i++) {
                CompoundTag nbt = componentList.getCompound(i);
                ComponentKey<?> type = ComponentRegistry.get(new Identifier(nbt.getString("componentId")));
                if (type != null) {
                    Component component = type.getInternal(this);
                    if (component != null) {
                        component.readFromNbt(nbt);
                    }
                }
            }
        } else if (tag.contains("cardinal_components", NbtType.COMPOUND)) {
            CompoundTag componentMap = tag.getCompound(NBT_KEY);
            for (String keyId : componentMap.getKeys()) {
                try {
                    ComponentKey<?> key = ComponentRegistry.get(new Identifier(keyId));
                    if (key != null) {
                        Component component = key.getInternal(this);
                        if (component != null) {
                            component.readFromNbt(componentMap.getCompound(keyId));
                        }
                    } else {
                        ComponentsInternals.LOGGER.warn("Failed to deserialize component: unregistered key " + keyId);
                    }
                } catch (InvalidIdentifierException e) {
                    ComponentsInternals.LOGGER.warn("Failed to deserialize component: invalid id " + keyId);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation first checks if the container is empty; if so it
     * returns immediately. Then, it iterates over this container's mappings, and creates
     * a compound tag for each component. The tag is then passed to the component's
     * {@link Component#writeToNbt(CompoundTag)} method. Every such serialized component is appended
     * to a {@code CompoundTag}, using the component type's identifier as the key.
     * The serialized map is finally appended to the passed in tag using the "cardinal_components" key.
     */
    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if(this.hasComponents()) {
            CompoundTag componentMap = null;
            CompoundTag componentTag = new CompoundTag();

            for (ComponentKey<?> type : this.keys()) {
                Component component = type.getFromContainer(this);
                component.writeToNbt(componentTag);

                if (!componentTag.isEmpty()) {
                    if (componentMap == null) {
                        componentMap = new CompoundTag();
                        tag.put(NBT_KEY, componentMap);
                    }

                    componentMap.put(type.getId().toString(), componentTag);
                    componentTag = new CompoundTag();   // recycle tag objects if possible
                }
            }
        }
        return tag;
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
