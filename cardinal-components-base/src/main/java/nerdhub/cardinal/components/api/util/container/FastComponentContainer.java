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
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.internal.SharedComponentSecrets;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * A {@link Component} container with a fast, small-footprint implementation
 * based on {@link Int2ObjectMap}
 *
 * <p> <b>Note that this implementation is not synchronized./b>
 * If multiple threads access an indexed container concurrently, and at least one of the threads
 * modifies the container structurally, it must be synchronized externally.
 * (A structural modification is any operation that adds one or more mappings;
 * merely changing the value associated with a key that an instance already contains is not
 * a structural modification.) This is typically accomplished by synchronizing on some object
 * that naturally encapsulates the container.
 *
 */
public final class FastComponentContainer<C extends Component> extends AbstractComponentContainer<C> {
    /**
     * All of the component types that can be stored in this container.
     * (Cached for performance.)
     */
    private final AtomicReference<ComponentType<?>[]> keyUniverse = SharedComponentSecrets.getRegisteredComponents();

    private final Int2ObjectOpenHashMap<C> vals;

    public FastComponentContainer() {
        this(Hash.DEFAULT_INITIAL_SIZE);
    }

    /**
     * @param expected the expected number of elements in the container
     */
    public FastComponentContainer(int expected) {
        this.vals = new Int2ObjectOpenHashMap<>(expected, Hash.VERY_FAST_LOAD_FACTOR);
    }

    /**
     * Returns the number of components held by this container.
     *
     * @return the number of components in this container
     */
    public int size() {
        return this.vals.size();
    }

    public boolean containsKey(ComponentType<?> key) {
        return this.vals.containsKey(key.getRawId());
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
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
        return this.vals.put(key.getRawId(), value);
    }

    @Override
    public void forEach(BiConsumer<? super ComponentType<?>, ? super C> action) {
        this.vals.int2ObjectEntrySet().fastForEach((e) ->
            action.accept(this.keyUniverse.get()[e.getIntKey()], e.getValue()));
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
        Set<ComponentType<?>> ks = keySet;
        if (ks != null) {
            return ks;
        }
        return keySet = new KeySet();
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
        Collection<C> vs = values;
        if (vs != null) {
            return vs;
        }
        return values = new Values();
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
    public Set<Map.Entry<ComponentType<?>, C>> entrySet() {
        Set<Map.Entry<ComponentType<?>,C>> es = entrySet;
        if (es != null) {
            return es;
        }
        return entrySet = new EntrySet();
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
            return key instanceof ComponentType && Objects.equals(entry.getValue(), get(key));
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
        public int size() {
            return FastComponentContainer.this.size();
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
            return FastComponentContainer.this.size();
        }
        public boolean contains(Object o) {
            return containsKey(o);
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
            return FastComponentContainer.this.size();
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    }

    private class ValueIterator implements Iterator<C> {
        ObjectIterator<C> it = vals.values().iterator();

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        public C next() {
            return it.next();
        }

    }

    private class KeyIterator implements Iterator<ComponentType<?>> {
        IntIterator it = vals.keySet().iterator();

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        public ComponentType<?> next() {
            return keyUniverse.get()[it.nextInt()];
        }

    }

    private class EntryIterator implements Iterator<Entry<ComponentType<?>, C>> {
        ObjectIterator<Int2ObjectMap.Entry<C>> it = vals.int2ObjectEntrySet().fastIterator();
        public Entry next() {
            Int2ObjectMap.Entry<C> entry = it.next();
            return new Entry(entry.getIntKey(), entry.getValue());
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        private class Entry implements Map.Entry<ComponentType<?>, C> {
            private int rawId;
            private C value;

            private Entry(int rawId, C value) {
                this.rawId = rawId;
                this.value = value;
            }

            public ComponentType<?> getKey() {
                return keyUniverse.get()[rawId];
            }

            public C getValue() {
                return value;
            }

            @Nullable
            public C setValue(C value) {
                return FastComponentContainer.this.put(getKey(), value);
            }

        }

    }

}
