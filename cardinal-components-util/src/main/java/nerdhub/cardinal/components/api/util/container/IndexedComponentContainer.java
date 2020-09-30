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
package nerdhub.cardinal.components.api.util.container;

import com.google.common.base.Preconditions;
import dev.onyxstudios.cca.internal.base.ComponentRegistryImpl;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * An efficient container for attached {@link Component components}.
 *
 * <p> This implementation is best suited for tightly packed numeric ids
 * (eg. component types registered together). Wide id ranges result
 * in a sparse collection with a higher memory requirement than
 * other implementations like {@link FastComponentContainer}.
 *
 * <p> <b>Note that this implementation is not synchronized.</b>
 * If multiple threads access an indexed container concurrently, and at least one of the threads
 * modifies the container structurally, it must be synchronized externally.
 * (A structural modification is any operation that adds one or more mappings;
 * merely changing the value associated with a key that an instance already contains is not
 * a structural modification.) This is typically accomplished by synchronizing on some object
 * that naturally encapsulates the container.
 *
 * @implNote The implementation is based on {@link java.util.EnumMap} and offers constant time
 * execution for all read operations. They may be faster than their {@link FastComponentContainer}
 * counterparts.
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public final class IndexedComponentContainer<C extends Component> extends AbstractComponentContainer<C> {
    private int universeSize;
    private int minIndex;

    /**
     * Array representation of this map.  The ith element is the value
     * to which universe[i] is currently mapped, or null if it isn't
     * mapped to anything.
     */
    private Component[] vals;

    /**
     * The number of mappings in this container.
     * This number should only ever increase.
     */
    private int size;

    /**
     * Creates a new {@code IndexedComponentContainer} using the settings from an existing one.
     * This operation will not copy the content of the container.
     * This method can be useful to create pre-sized containers from an existing model.
     */
    public static <C extends Component> IndexedComponentContainer<C> withSettingsFrom(IndexedComponentContainer<C> other) {
        IndexedComponentContainer<C> ret = new IndexedComponentContainer<>();
        ret.universeSize = other.universeSize;
        ret.vals = new Component[other.universeSize];
        return ret;
    }

    public IndexedComponentContainer() {
        this.universeSize = 0;
        this.minIndex = 0;
        this.size = 0;
        this.vals = new Component[0];
    }

    /**
     * Returns the number of components held by this container.
     *
     * @return the number of components in this container
     */
    public int size() {
        return this.size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(@Nullable Object key) {
        if (key != null && key.getClass() == ComponentType.class) {
            return this.containsKey((ComponentType<?>) key);
        }
        return false;
    }

    public boolean containsKey(ComponentType<?> key) {
        final int index = key.getRawId() - this.minIndex;
        Component[] vals = this.vals;
        return index >= 0 && index < vals.length && vals[index] != null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public C get(@Nullable Object key) {
        if (key != null && key.getClass() == ComponentType.class) {
            return (C) this.get((ComponentType<?>) key);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> T get(ComponentType<T> key) {
        final int index = key.getRawId() - this.minIndex;
        Component[] vals = this.vals;
        return index >= 0 && index < vals.length ? (T) vals[index] : null;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public C put(ComponentType<?> key, C value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        Preconditions.checkArgument(key.getComponentClass().isInstance(value), value + " is not of type " + key);
        Preconditions.checkState(!this.containsKey(key), "Cannot register 2 components for " + key);
        Component[] vals = this.vals;
        final int rawId = key.getRawId();
        int index = rawId - this.minIndex;
        if (index < 0 || index >= this.universeSize) {
            // update the underlying component array to accept the new component
            if (this.universeSize == 0) this.minIndex = rawId;
            int newMinIndex = Math.min(this.minIndex, rawId);
            int newUniverseSize = Math.max(this.universeSize + this.minIndex, rawId+1) - newMinIndex;

            assert newUniverseSize > this.universeSize : "universe must expand when resized during put operation";

            vals = new Component[newUniverseSize];
            assert this.minIndex >= newMinIndex : "minimum index cannot increase";
            System.arraycopy(this.vals, 0, vals, this.minIndex - newMinIndex, this.vals.length);
            this.vals = vals;
            this.minIndex = newMinIndex;
            this.universeSize = newUniverseSize;
            index = rawId - this.minIndex; // compute index again since min index changed
        }
        @SuppressWarnings("unchecked") C oldValue = (C) vals[index];
        vals[index] = value;
        assert vals[0] != null && vals[this.universeSize -1] != null;
        if (oldValue == null) {
            this.size++;
        }
        return oldValue;
    }

    @Override
    public void forEach(BiConsumer<? super ComponentType<?>, ? super C> action) {
        Component[] vals = this.vals;
        for (int i = 0; i < vals.length; i++) {
            @SuppressWarnings("unchecked") C val = (C) vals[i];
            if (val != null) {
                action.accept(ComponentRegistryImpl.byRawId(this.minIndex + i), val);
            }
        }
    }

    // IndexedComponentContainer specific methods

    /**
     * The size of the current range of values that this container uses.
     */
    public int getUniverseSize() {
        return this.universeSize;
    }

    // Views

    /**
     * This field is initialized to contain an instance of the entry set
     * view the first time this view is requested.  The view is stateless,
     * so there's no reason to create more than one.
     */
    private Set<Map.Entry<ComponentType<?>, C>> entrySet;
    private Set<ComponentType<?>> keySet;
    private Collection<C> values;

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The returned set obeys the general contract outlined in
     * {@link Map#keySet()}.  The set's iterator will return the keys
     * in their registration order.
     *
     * @return a set view of the keys contained in this map
     */
    public Set<ComponentType<?>> keySet() {
        Set<ComponentType<?>> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        return this.keySet = new KeySet();
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The returned collection obeys the general contract outlined in
     * {@link Map#values()}.  The collection's iterator will return the
     * values in the order their corresponding keys appear in map,
     * which is their natural order (the order in which the component types
     * are registered).
     *
     * @return a collection view of the values contained in this map
     */
    @Override
    public Collection<C> values() {
        Collection<C> vs = this.values;
        if (vs != null) {
            return vs;
        }
        return this.values = new Values();
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The returned set obeys the general contract outlined in
     * {@link Map#keySet()}.  The set's iterator will return the
     * mappings in the order their keys appear in map, which is their
     * natural order (the order in which the component types are registered).
     *
     * @return a set view of the mappings contained in this container
     */
    public Set<Map.Entry<ComponentType<?>, C>> entrySet() {
        Set<Map.Entry<ComponentType<?>,C>> es = this.entrySet;
        if (es != null) {
            return es;
        }
        return this.entrySet = new EntrySet();
    }

    private class EntrySet extends AbstractSet<Map.Entry<ComponentType<?>,C>> {
        public Iterator<Map.Entry<ComponentType<?>, C>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
            Object key = entry.getKey();
            return key instanceof ComponentType && Objects.equals(entry.getValue(), IndexedComponentContainer.this.get(key));
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
        public int size() {
            return IndexedComponentContainer.this.size;
        }
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    private class KeySet extends AbstractSet<ComponentType<?>> {
        public Iterator<ComponentType<?>> iterator() {
            return new KeyIterator();
        }
        public int size() {
            return IndexedComponentContainer.this.size;
        }
        public boolean contains(Object o) {
            return IndexedComponentContainer.this.containsKey(o);
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    }

    private class Values extends AbstractCollection<C> {
        public Iterator<C> iterator() {
            return new ValueIterator();
        }
        public int size() {
            return IndexedComponentContainer.this.size;
        }
        public boolean contains(Object o) {
            return IndexedComponentContainer.this.containsValue(o);
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    }

    private abstract class ComponentContainerIterator<T> implements Iterator<T> {
        // Lower bound on index of next element to return
        int index = 0;

        public boolean hasNext() {
            while (this.index < IndexedComponentContainer.this.vals.length && IndexedComponentContainer.this.vals[this.index] == null)
                this.index++;
            return this.index != IndexedComponentContainer.this.vals.length;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class KeyIterator extends ComponentContainerIterator<ComponentType<?>> {
        public ComponentType<?> next() {
            if (!this.hasNext())
                throw new NoSuchElementException();
            return ComponentRegistryImpl.byRawId(IndexedComponentContainer.this.minIndex + this.index++);
        }
    }

    private class ValueIterator extends ComponentContainerIterator<C> {
        @SuppressWarnings("unchecked")
        public C next() {
            if (!this.hasNext()) throw new NoSuchElementException();
            return (C) IndexedComponentContainer.this.vals[this.index++];
        }
    }

    private class EntryIterator extends ComponentContainerIterator<Entry<ComponentType<?>, C>> {
        public Entry next() {
            if (!this.hasNext()) throw new NoSuchElementException();
            return new Entry(this.index++);
        }


        private class Entry implements Map.Entry<ComponentType<?>, C> {
            private final int index;

            private Entry(int index) {
                this.index = index;
            }

            public ComponentType<?> getKey() {
                return ComponentRegistryImpl.byRawId(IndexedComponentContainer.this.minIndex + this.index);
            }

            @SuppressWarnings("unchecked")
            public C getValue() {
                return (C) IndexedComponentContainer.this.vals[this.index];
            }

            @Nullable
            public C setValue(C value) {
                return IndexedComponentContainer.this.put(this.getKey(), value);
            }

            public boolean equals(Object o) {
                if (this.index < 0)
                    return o == this;

                if (!(o instanceof Map.Entry))
                    return false;

                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                Component ourValue = IndexedComponentContainer.this.vals[this.index];
                Object hisValue = e.getValue();
                return (e.getKey() == ComponentRegistryImpl.byRawId(IndexedComponentContainer.this.minIndex + this.index) && Objects.equals(ourValue, hisValue));
            }

            public int hashCode() {
                return (ComponentRegistryImpl.byRawId(IndexedComponentContainer.this.minIndex + this.index).hashCode() ^ IndexedComponentContainer.this.vals[this.index].hashCode());
            }

            public String toString() {
                return ComponentRegistryImpl.byRawId(IndexedComponentContainer.this.minIndex + this.index) + "=" + IndexedComponentContainer.this.vals[this.index];
            }

        }

    }

}
