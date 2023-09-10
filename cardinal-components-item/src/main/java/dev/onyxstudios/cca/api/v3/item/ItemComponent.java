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
package dev.onyxstudios.cca.api.v3.item;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Base implementation for an {@link ItemStack} component that stores data in the {@linkplain ItemStack#getNbt() stack NBT}.
 *
 * @see ItemComponentFactoryRegistry#register(Item, ComponentKey, ComponentFactory)
 * @see ItemComponentFactoryRegistry#register(Predicate, ComponentKey, ComponentFactory)
 */
public abstract class ItemComponent implements Component, ItemTagInvalidationListener {
    private @Nullable NbtCompound rootTag;
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
     * <p>The returned tag is a {@linkplain ItemStack#getSubNbt(String)} subtag} attached to this component's stack
     * (the stack to which this component is attached).
     * The subtag is mapped to this component's {@linkplain #getRootTagKey() root key}.
     *
     * @return the tag storing this component's data, or {@code null} if it does not exist
     */
    protected @Nullable NbtCompound getRootTag() {
        return this.rootTag;
    }

    /**
     * Returns the tag storing this component's data, creating it if it does not exist.
     *
     * <p>The returned tag is a {@linkplain ItemStack#getOrCreateSubNbt(String) subtag} attached to this component's stack
     * (the stack to which this component is attached).
     * The subtag is mapped to this component's {@linkplain #getRootTagKey() root key}.
     *
     * @return the tag storing this component's data
     */
    protected NbtCompound getOrCreateRootTag() {
        if (this.rootTag != null) return this.rootTag;
        return this.rootTag = this.stack.getOrCreateSubNbt(this.getRootTagKey());
    }

    /**
     * @see NbtCompound#getBoolean(String)
     */
    protected boolean getBoolean(String key) {
        NbtCompound rootTag = this.getRootTag();
        return rootTag != null && rootTag.getBoolean(key);
    }

    /**
     * @see NbtCompound#getInt(String)
     */
    protected int getInt(String key) {
        NbtCompound rootTag = this.getRootTag();
        return rootTag == null ? 0 : rootTag.getInt(key);
    }

    /**
     * @see NbtCompound#getLong(String)
     */
    protected long getLong(String key) {
        NbtCompound rootTag = this.getRootTag();
        return rootTag == null ? 0 : rootTag.getLong(key);
    }

    /**
     * @see NbtCompound#getFloat(String)
     */
    protected float getFloat(String key) {
        NbtCompound rootTag = this.getRootTag();
        return rootTag == null ? 0 : rootTag.getFloat(key);
    }

    /**
     * @see NbtCompound#getDouble(String)
     */
    protected double getDouble(String key) {
        NbtCompound rootTag = this.getRootTag();
        return rootTag == null ? 0 : rootTag.getDouble(key);
    }

    /**
     * @see NbtCompound#getString(String)
     */
    protected String getString(String key) {
        NbtCompound rootTag = this.getRootTag();
        return rootTag == null ? "" : rootTag.getString(key);
    }

    /**
     * @see NbtCompound#getList(String, int)
     */
    protected <T extends NbtElement> List<T> getList(String key, CcaNbtType<T> type) {
        NbtCompound rootTag = this.getRootTag();
        @SuppressWarnings("unchecked") List<T> ts = rootTag == null ? Collections.emptyList() : (List<T>) rootTag.getList(key, type.getId());
        return ts;
    }

    /**
     * @see NbtCompound#getList(String, int)
     * @see NbtType
     */
    protected NbtList getList(String key, int type) {
        NbtCompound rootTag = this.getRootTag();
        return rootTag == null ? new NbtList() : rootTag.getList(key, type);
    }

    /**
     * @see NbtCompound#getCompound(String)
     */
    protected NbtCompound getCompound(String key) {
        NbtCompound rootTag = this.getRootTag();
        return rootTag == null ? new NbtCompound() : rootTag.getCompound(key);
    }

    /**
     * @see NbtCompound#getUuid(String)
     */
    protected @Nullable UUID getUuid(String key) {
        NbtCompound rootTag = this.getRootTag();
        return rootTag != null && rootTag.containsUuid(key) ? rootTag.getUuid(key) : null;
    }

    /**
     * @see NbtCompound#putBoolean(String, boolean)
     */
    protected void putBoolean(String key, boolean value) {
        if (value) {
            this.getOrCreateRootTag().putBoolean(key, true);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see NbtCompound#putInt(String, int)
     */
    protected void putInt(String key, int value) {
        if (value != 0) {
            this.getOrCreateRootTag().putInt(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see NbtCompound#putLong(String, long)
     */
    protected void putLong(String key, long value) {
        if (value != 0) {
            this.getOrCreateRootTag().putLong(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see NbtCompound#putFloat(String, float)
     */
    protected void putFloat(String key, float value) {
        if (value != 0) {
            this.getOrCreateRootTag().putFloat(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see NbtCompound#putDouble(String, double)
     */
    protected void putDouble(String key, double value) {
        if (value != 0) {
            this.getOrCreateRootTag().putDouble(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see NbtCompound#putString(String, String)
     */
    protected void putString(String key, String value) {
        if (!value.isEmpty()) {
            this.getOrCreateRootTag().putString(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see NbtCompound#put(String, NbtElement)
     */
    protected void putList(String key, NbtList value) {
        if (!value.isEmpty()) {
            this.getOrCreateRootTag().put(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see NbtCompound#put(String, NbtElement)
     */
    protected void putCompound(String key, NbtCompound value) {
        if (!value.isEmpty()) {
            this.getOrCreateRootTag().put(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * @see NbtCompound#putUuid(String, UUID)
     */
    protected void putUuid(String key, UUID value) {
        this.getOrCreateRootTag().putUuid(key, value);
    }

    /**
     * @see NbtCompound#remove(String)
     */
    protected void remove(String key) {
        NbtCompound rootTag = this.getRootTag();

        if (rootTag != null) {
            rootTag.remove(key);

            if (rootTag.isEmpty()) {
                this.stack.removeSubNbt(this.getRootTagKey());
                this.rootTag = null;
            }
        }
    }

    /**
     * @return {@code true} if the {@link NbtCompound} storing this component's data has a subtag with the given {@code key},
     * {@code false} otherwise
     * @see NbtCompound#contains(String)
     */
    protected boolean hasTag(String key) {
        NbtCompound rootTag = this.getRootTag();
        return rootTag != null && rootTag.contains(key);
    }

    /**
     * @return {@code true} if the {@link NbtCompound} storing this component's data has a subtag with the given {@code key}
     * and of the appropriate {@code type}, {@code false} otherwise
     * @see NbtCompound#contains(String, int)
     * @see NbtType
     */
    protected boolean hasTag(String key, int type) {
        NbtCompound rootTag = this.getRootTag();
        return rootTag != null && rootTag.contains(key, type);
    }

    /**
     * @return {@code true} if the {@link NbtCompound} storing this component's data has a subtag with the given {@code key}
     * and of the appropriate {@code type}, {@code false} otherwise
     * @see NbtCompound#contains(String, int)
     */
    protected boolean hasTag(String key, CcaNbtType<?> type) {
        return this.hasTag(key, type.getId());
    }

    /**
     * @return the subtag with the given {@code key} from the {@link NbtCompound} storing this component's data,
     * or {@code null} if no such tag exists
     * @see NbtCompound#get(String)
     */
    protected @Nullable NbtElement getTag(String key) {
        NbtCompound rootTag = this.getRootTag();
        if (rootTag == null) return null;
        return rootTag.get(key);
    }

    /**
     * @return the subtag with the given {@code key} from the {@link NbtCompound} storing this component's data,
     * or {@code null} if no such tag exists or is not of the right {@code type}
     * @see NbtCompound#get(String)
     * @see NbtType
     */
    protected @Nullable NbtElement getTag(String key, int type) {
        NbtElement tag = this.getTag(key);
        if (tag == null || tag.getType() != type) return null;
        return tag;
    }

    /**
     * @return the subtag with the given {@code key} from the {@link NbtCompound} storing this component's data,
     * or {@code null} if no such tag exists or is not of the right {@code type}
     * @see NbtCompound#get(String)
     */
    protected <T extends NbtElement> @Nullable T getTag(String key, CcaNbtType<T> type) {
        @SuppressWarnings("unchecked") T ret = (T) this.getTag(key, type.getId());
        return ret;
    }

    /**
     * @see NbtCompound#getKeys()
     */
    protected Set<String> getKeys() {
        NbtCompound rootTag = this.getRootTag();
        return rootTag == null ? Collections.emptySet() : rootTag.getKeys();
    }

    @ApiStatus.Experimental
    @Override
    public void onTagInvalidated() {
        this.rootTag = this.stack.getSubNbt(this.getRootTagKey());
    }

    @Deprecated
    @Override
    public final void readFromNbt(NbtCompound tag) {
        // Port from older external data
        this.getOrCreateRootTag().copyFrom(tag);
    }

    @Deprecated
    @Override
    public final void writeToNbt(NbtCompound tag) {
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
