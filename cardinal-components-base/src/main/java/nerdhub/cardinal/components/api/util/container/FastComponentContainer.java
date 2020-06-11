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
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * A {@link Component} container with a fast, small-footprint implementation
 * based on {@link Int2ObjectMap}
 *
 * <p> <b>Note that this implementation is not synchronized.</b>
 * If multiple threads access an indexed container concurrently, and at least one of the threads
 * modifies the container structurally, it must be synchronized externally.
 * (A structural modification is any operation that adds one or more mappings;
 * merely changing the value associated with a key that an instance already contains is not
 * a structural modification.) This is typically accomplished by synchronizing on some object
 * that naturally encapsulates the container.
 *
 */
public class FastComponentContainer<C extends Component> extends AbstractComponentContainer<C> {
    private final BitSet containedTypes;
    private final Int2ObjectOpenHashMap<C> vals;

    public FastComponentContainer() {
        this(Hash.DEFAULT_INITIAL_SIZE);
    }

    /**
     * @param expected the expected number of <em>dynamically added</em> elements in the container
     */
    public FastComponentContainer(int expected) {
        this.containedTypes = new BitSet(((ComponentRegistryImpl) ComponentRegistry.INSTANCE).size());
        this.vals = new Int2ObjectOpenHashMap<>(expected, Hash.VERY_FAST_LOAD_FACTOR);
    }

    @SuppressWarnings("unused") // called by generated subclasses
    protected void addContainedType(ComponentType<?> type) {
        this.containedTypes.set(type.getRawId());
    }

    /**
     * Returns the number of components held by this container.
     *
     * @return the number of components in this container
     */
    @Override
    public int size() {
        return this.containedTypes.cardinality();
    }

    @SuppressWarnings("unused") // called by generated factories to adjust the initial size of future containers
    public final int dynamicSize() {
        return this.vals.size();
    }

    @Override
    public boolean containsKey(ComponentType<?> key) {
        return this.containedTypes.get(key.getRawId());
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override   // overridden by generated subclasses
    @SuppressWarnings("unchecked")
    public <T extends Component> T get(ComponentType<T> key) {
        return (T) this.vals.get(key.getRawId());
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
        Preconditions.checkState(this.canBeAssigned(key), "Component type " + key + " was already defined with value " + this.get(key) + ", cannot replace with " + value);
        this.containedTypes.set(key.getRawId());
        return this.vals.put(key.getRawId(), value);
    }

    // overridden by generated subclasses
    protected boolean canBeAssigned(ComponentType<?> key) {
        return !this.containsKey(key);
    }

    @Override   // overridden by generated subclasses
    public void forEach(BiConsumer<? super ComponentType<?>, ? super C> action) {
        this.vals.int2ObjectEntrySet().fastForEach((e) ->
            action.accept(ComponentRegistryImpl.byRawId(e.getIntKey()), e.getValue()));
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
    @Override
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
     * which is their natural order (the order in which the enum constants
     * are declared).
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
     * natural order (the order in which the enum constants are declared).
     *
     * @return a set view of the mappings contained in this enum map
     */
    @Override
    public Set<Map.Entry<ComponentType<?>, C>> entrySet() {
        Set<Map.Entry<ComponentType<?>,C>> es = this.entrySet;
        if (es != null) {
            return es;
        }
        return this.entrySet = new EntrySet();
    }

    private class EntrySet extends AbstractSet<Map.Entry<ComponentType<?>,C>> {
        @Override
        public Iterator<Map.Entry<ComponentType<?>, C>> iterator() {
            return new EntryIterator();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
            Object key = entry.getKey();
            return key instanceof ComponentType && Objects.equals(entry.getValue(), FastComponentContainer.this.get(key));
        }
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
        @Override
        public int size() {
            return FastComponentContainer.this.size();
        }
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    private class KeySet extends AbstractSet<ComponentType<?>> {
        @Override
        public Iterator<ComponentType<?>> iterator() {
            return new KeyIterator();
        }
        @Override
        public int size() {
            return FastComponentContainer.this.size();
        }
        @Override
        public boolean contains(Object o) {
            return FastComponentContainer.this.containsKey(o);
        }
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    }

    private class Values extends AbstractCollection<C> {
        @Override
        public Iterator<C> iterator() {
            return new ValueIterator();
        }
        @Override
        public int size() {
            return FastComponentContainer.this.size();
        }
        @Override
        public boolean contains(Object o) {
            return FastComponentContainer.this.containsValue(o);
        }
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    }

    private class ValueIterator implements Iterator<C> {
        private boolean advance = true;
        private int curId = -1;

        @Override
        public boolean hasNext() {
            if (this.advance) {
                this.curId = FastComponentContainer.this.containedTypes.nextSetBit(this.curId + 1);
                this.advance = false;
            }
            return this.curId >= 0;
        }

        @Override
        public C next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.advance = true;
            ComponentType<?> key = ComponentRegistryImpl.byRawId(this.curId);
            @SuppressWarnings("unchecked") C value = (C) FastComponentContainer.this.get(key);
            assert value != null;
            return value;
        }

    }

    private final class KeyIterator implements Iterator<ComponentType<?>> {
        private boolean advance = true;
        private int curId = -1;

        @Override
        public boolean hasNext() {
            if (this.advance) {
                this.curId = FastComponentContainer.this.containedTypes.nextSetBit(this.curId + 1);
                this.advance = false;
            }
            return this.curId >= 0;
        }

        @Override
        public ComponentType<?> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.advance = true;
            return ComponentRegistryImpl.byRawId(this.curId);
        }
    }

    private final class EntryIterator implements Iterator<Entry<ComponentType<?>, C>> {
        private boolean advance = true;
        private int curId = -1;

        @Override
        public boolean hasNext() {
            if (this.advance) {
                this.curId = FastComponentContainer.this.containedTypes.nextSetBit(this.curId + 1);
                this.advance = false;
            }
            return this.curId >= 0;
        }

        @Override
        public Entry next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.advance = true;
            ComponentType<?> key = ComponentRegistryImpl.byRawId(this.curId);
            @SuppressWarnings("unchecked") C value = (C) FastComponentContainer.this.get(key);
            assert value != null;
            return new Entry(key, value);
        }

        private final class Entry implements Map.Entry<ComponentType<?>, C> {
            private final ComponentType<?> key;
            private final C value;

            private Entry(ComponentType<?> key, C value) {
                this.key = key;
                this.value = value;
            }

            @Override
            public ComponentType<?> getKey() {
                return this.key;
            }

            @Override
            public C getValue() {
                return this.value;
            }

            @Nullable
            @Override
            public C setValue(C value) {
                return FastComponentContainer.this.put(this.getKey(), value);
            }
        }
    }
}
