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
package dev.onyxstudios.cca.api.v3.item;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Base implementation for an {@link ItemStack} component that stores data in the {@linkplain ItemStack#getTag() stack NBT}.
 *
 * @see ItemComponentFactoryRegistry#register(Item, ComponentKey, ComponentFactory)
 * @see ItemComponentFactoryRegistry#register(Predicate, ComponentKey, ComponentFactory)
 */
public abstract class ItemComponent implements Component, ItemTagInvalidationListener {
    private @Nullable CompoundTag rootTag;
    protected final ItemStack stack;
    private String rootTagKey;

    public ItemComponent(ItemStack stack) {
        this.stack = stack;
    }

    public ItemComponent(ItemStack stack, ComponentKey<?> key) {
        this(stack);
        this.setRootTagKey(key.getId().toString());
    }

    protected String getRootTagKey() {
        return this.rootTagKey;
    }

    /**
     * Returns the tag storing this component's data.
     *
     * <p>The returned tag is a {@linkplain ItemStack#getSubTag(String)} subtag} attached to this component's stack
     * (the stack to which this component is attached).
     * The subtag is mapped to this component's {@linkplain #getRootTagKey() root key}.
     *
     * @return the tag storing this component's data, or {@code null} if it does not exist
     */
    protected @Nullable CompoundTag getRootTag() {
        return this.rootTag;
    }

    /**
     * Returns the tag storing this component's data, creating it if it does not exist.
     *
     * <p>The returned tag is a {@linkplain ItemStack#getOrCreateSubTag(String) subtag} attached to this component's stack
     * (the stack to which this component is attached).
     * The subtag is mapped to this component's {@linkplain #getRootTagKey() root key}.
     *
     * @return the tag storing this component's data
     */
    protected CompoundTag getOrCreateRootTag() {
        if (this.rootTag != null) return this.rootTag;
        return this.rootTag = this.stack.getOrCreateSubTag(this.getRootTagKey());
    }

    /**
     * @see CompoundTag#getBoolean(String)
     */
    protected boolean getBoolean(String key) {
        CompoundTag rootTag = this.getRootTag();
        return rootTag != null && rootTag.getBoolean(key);
    }

    /**
     * @see CompoundTag#getInt(String)
     */
    protected int getInt(String key) {
        CompoundTag rootTag = this.getRootTag();
        return rootTag == null ? 0 : rootTag.getInt(key);
    }

    /**
     * @see CompoundTag#getLong(String)
     */
    protected long getLong(String key) {
        CompoundTag rootTag = this.getRootTag();
        return rootTag == null ? 0 : rootTag.getLong(key);
    }

    /**
     * @see CompoundTag#getFloat(String)
     */
    protected float getFloat(String key) {
        CompoundTag rootTag = this.getRootTag();
        return rootTag == null ? 0 : rootTag.getFloat(key);
    }

    /**
     * @see CompoundTag#getDouble(String)
     */
    protected double getDouble(String key) {
        CompoundTag rootTag = this.getRootTag();
        return rootTag == null ? 0 : rootTag.getDouble(key);
    }

    /**
     * @see CompoundTag#getString(String)
     */
    protected String getString(String key) {
        CompoundTag rootTag = this.getRootTag();
        return rootTag == null ? "" : rootTag.getString(key);
    }

    /**
     * @see CompoundTag#getList(String, int)
     */
    protected <T extends Tag> List<T> getList(String key, CcaNbtType<T> type) {
        CompoundTag rootTag = this.getRootTag();
        @SuppressWarnings("unchecked") List<T> ts = rootTag == null ? Collections.emptyList() : (List<T>) rootTag.getList(key, type.getId());
        return ts;
    }

    /**
     * @see CompoundTag#getList(String, int)
     * @see net.fabricmc.fabric.api.util.NbtType
     */
    protected ListTag getList(String key, int type) {
        CompoundTag rootTag = this.getRootTag();
        return rootTag == null ? new ListTag() : rootTag.getList(key, type);
    }

    /**
     * @see CompoundTag#getCompound(String)
     */
    protected CompoundTag getCompound(String key) {
        CompoundTag rootTag = this.getRootTag();
        return rootTag == null ? new CompoundTag() : rootTag.getCompound(key);
    }

    /**
     * @see CompoundTag#getUuid(String)
     */
    protected @Nullable UUID getUuid(String key) {
        CompoundTag rootTag = this.getRootTag();
        return rootTag != null && rootTag.containsUuid(key) ? rootTag.getUuid(key) : null;
    }

    /**
     * @see CompoundTag#putBoolean(String, boolean)
     */
    protected void putBoolean(String key, boolean value) {
        if (value) {
            this.getOrCreateRootTag().putBoolean(key, true);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see CompoundTag#putInt(String, int)
     */
    protected void putInt(String key, int value) {
        if (value != 0) {
            this.getOrCreateRootTag().putInt(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see CompoundTag#putLong(String, long)
     */
    protected void putLong(String key, long value) {
        if (value != 0) {
            this.getOrCreateRootTag().putLong(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see CompoundTag#putFloat(String, float)
     */
    protected void putFloat(String key, float value) {
        if (value != 0) {
            this.getOrCreateRootTag().putFloat(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see CompoundTag#putDouble(String, double)
     */
    protected void putDouble(String key, double value) {
        if (value != 0) {
            this.getOrCreateRootTag().putDouble(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see CompoundTag#putString(String, String)
     */
    protected void putString(String key, String value) {
        if (!value.isEmpty()) {
            this.getOrCreateRootTag().putString(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see CompoundTag#put(String, Tag)
     */
    protected void putList(String key, ListTag value) {
        if (!value.isEmpty()) {
            this.getOrCreateRootTag().put(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see CompoundTag#put(String, Tag)
     */
    protected void putCompound(String key, CompoundTag value) {
        if (!value.isEmpty()) {
            this.getOrCreateRootTag().put(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see CompoundTag#putUuid(String, UUID)
     */
    protected void putUuid(String key, UUID value) {
        this.getOrCreateRootTag().putUuid(key, value);
    }

    /**
     * @see CompoundTag#remove(String)
     */
    protected void remove(String key) {
        CompoundTag rootTag = this.getRootTag();

        if (rootTag != null) {
            rootTag.remove(key);

            if (rootTag.isEmpty()) {
                this.stack.removeSubTag(this.getRootTagKey());
                this.rootTag = null;
            }
        }
    }

    /**
     * @return {@code true} if the {@link CompoundTag} storing this component's data has a subtag with the given {@code key},
     * {@code false} otherwise
     * @see CompoundTag#contains(String)
     */
    protected boolean hasTag(String key) {
        CompoundTag rootTag = this.getRootTag();
        return rootTag != null && rootTag.contains(key);
    }

    /**
     * @return {@code true} if the {@link CompoundTag} storing this component's data has a subtag with the given {@code key}
     * and of the appropriate {@code type}, {@code false} otherwise
     * @see CompoundTag#contains(String, int)
     * @see NbtType
     */
    protected boolean hasTag(String key, int type) {
        CompoundTag rootTag = this.getRootTag();
        return rootTag != null && rootTag.contains(key, type);
    }

    /**
     * @return {@code true} if the {@link CompoundTag} storing this component's data has a subtag with the given {@code key}
     * and of the appropriate {@code type}, {@code false} otherwise
     * @see CompoundTag#contains(String, int)
     */
    protected boolean hasTag(String key, CcaNbtType<?> type) {
        return this.hasTag(key, type.getId());
    }

    /**
     * @return the subtag with the given {@code key} from the {@link CompoundTag} storing this component's data,
     * or {@code null} if no such tag exists
     * @see CompoundTag#get(String)
     */
    protected @Nullable Tag getTag(String key) {
        CompoundTag rootTag = this.getRootTag();
        if (rootTag == null) return null;
        return rootTag.get(key);
    }

    /**
     * @return the subtag with the given {@code key} from the {@link CompoundTag} storing this component's data,
     * or {@code null} if no such tag exists or is not of the right {@code type}
     * @see CompoundTag#get(String)
     * @see NbtType
     */
    protected @Nullable Tag getTag(String key, int type) {
        Tag tag = this.getTag(key);
        if (tag == null || tag.getType() != type) return null;
        return tag;
    }

    /**
     * @return the subtag with the given {@code key} from the {@link CompoundTag} storing this component's data,
     * or {@code null} if no such tag exists or is not of the right {@code type}
     * @see CompoundTag#get(String)
     */
    protected <T extends Tag> @Nullable T getTag(String key, CcaNbtType<T> type) {
        @SuppressWarnings("unchecked") T ret = (T) this.getTag(key, type.getId());
        return ret;
    }

    /**
     * @see CompoundTag#getKeys()
     */
    protected Set<String> getKeys() {
        CompoundTag rootTag = this.getRootTag();
        return rootTag == null ? Collections.emptySet() : rootTag.getKeys();
    }

    @ApiStatus.Experimental
    @Override
    public void onTagInvalidated() {
        this.rootTag = this.stack.getSubTag(this.getRootTagKey());
    }

    @Deprecated
    @Override
    public final void readFromNbt(CompoundTag tag) {
        // Port from older external data
        this.getOrCreateRootTag().copyFrom(tag);
    }

    @Deprecated
    @Override
    public final void writeToNbt(CompoundTag tag) {
        // NO-OP
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        // all the actual data is in the stack and therefore already checked
        return obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return 487;
    }

    private void setRootTagKey(String rootTagKey) {
        this.rootTagKey = rootTagKey;
        this.onTagInvalidated();
    }

    @ApiStatus.Internal
    public static <C extends ItemComponent> ComponentFactory<ItemStack, C> wrapFactory(ComponentKey<? super C> key, ComponentFactory<ItemStack, C> factory) {
        String rootTagKey = key.getId().toString();
        return i -> {
            C c = factory.createComponent(i);
            ((ItemComponent) c).setRootTagKey(rootTagKey);
            return c;
        };
    }
}
