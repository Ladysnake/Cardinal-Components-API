package nerdhub.cardinal.components.api.util.impl;

import com.google.common.base.Preconditions;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * An efficient container for attached {@link Component}.
 *
 * @implNote The implementation is based on {@link java.util.EnumMap} and offers constant time
 * execution for all operations.
 */
public final class IndexedComponentContainer extends AbstractComponentContainer {
    /**
     * All of the component types that can be stored in this container.
     * (Cached for performance.)
     */
    private ComponentType<?>[] keyUniverse;

    private int universeSize;

    /**
     * Array representation of this map.  The ith element is the value
     * to which universe[i] is currently mapped, or null if it isn't
     * mapped to anything.
     */
    private Component[] vals;

    /**
     * The number of mappings in this container.
     */
    private int size;

    public IndexedComponentContainer() {
        this.universeSize = 0;
        this.keyUniverse = new ComponentType[universeSize];
        this.size = 0;
        this.vals = new Component[size];
    }

    public IndexedComponentContainer(ComponentContainer original) {
        this.putAll(original);
    }

    /**
     * Returns the number of components held by this container.
     *
     * @return the number of components in this container
     */
    public int size() {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object type) {
        final int rawId = ((ComponentType)type).getRawId();
        final Component[] vals = this.vals;
        return rawId < vals.length && vals[rawId] != null;
    }

    @Override
    public Component get(Object key) {
        return get((ComponentType<?>) key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> T get(ComponentType<T> key) {
        final int rawId = key.getRawId();
        final Component[] vals = this.vals;
        return rawId < vals.length ? (T) vals[rawId] : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V extends Component> V put(ComponentType<V> key, V value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        Preconditions.checkArgument(key.getComponentClass().isInstance(value), value + " is not of type " + key);
        Component[] vals = this.vals;
        int index = key.getRawId();
        if (index >= vals.length) {
            // update the underlying component array to accept the new component
            this.keyUniverse = ComponentRegistry.stream().toArray(ComponentType[]::new);
            universeSize = keyUniverse.length;
            vals = new Component[universeSize];
            System.arraycopy(this.vals, 0, vals, 0, this.vals.length);
            this.vals = vals;
        }
        V oldValue = key.getComponentClass().cast(vals[index]);
        vals[index] = value;
        if (oldValue == null) {
            size++;
        }
        return oldValue;
    }

    @Override
    public void forEach(BiConsumer<? super ComponentType<?>, ? super Component> action) {
        for (int i = 0; i < universeSize; i++) {
            Component val = vals[i];
            if (val != null) {
                action.accept(keyUniverse[i], val);
            }
        }
    }

    // Views

    /**
     * This field is initialized to contain an instance of the entry set
     * view the first time this view is requested.  The view is stateless,
     * so there's no reason to create more than one.
     */
    private Set<Map.Entry<ComponentType<?>, Component>> entrySet;
    private Set<ComponentType<?>> keySet;
    private Collection<Component> values;

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
    public Collection<Component> values() {
        Collection<Component> vs = values;
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
    public Set<Map.Entry<ComponentType<?>, Component>> entrySet() {
        Set<Map.Entry<ComponentType<?>,Component>> es = entrySet;
        if (es != null) {
            return es;
        }
        return entrySet = new EntrySet();
    }

    private class EntrySet extends AbstractSet<Map.Entry<ComponentType<?>,Component>> {
        public Iterator<Map.Entry<ComponentType<?>,Component>> iterator() {
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
            return size;
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
            return size;
        }
        public boolean contains(Object o) {
            return containsKey(o);
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    }

    private class Values extends AbstractCollection<Component> {
        public Iterator<Component> iterator() {
            return new ValueIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    }

    private abstract class ComponentContainerIterator<T> implements Iterator<T> {
        // Lower bound on index of next element to return
        int index = 0;

        public boolean hasNext() {
            while (index < vals.length && vals[index] == null)
                index++;
            return index != vals.length;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class KeyIterator extends ComponentContainerIterator<ComponentType<?>> {
        public ComponentType<?> next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return keyUniverse[index++];
        }
    }

    private class ValueIterator extends ComponentContainerIterator<Component> {
        public Component next() {
            if (!hasNext()) throw new NoSuchElementException();
            return vals[index++];
        }
    }

    private class EntryIterator extends ComponentContainerIterator<Entry<ComponentType<?>, Component>> {
        public Entry next() {
            if (!hasNext()) throw new NoSuchElementException();
            return new Entry(index++);
        }


        private class Entry implements Map.Entry<ComponentType<?>, Component> {
            private int index;

            private Entry(int index) {
                this.index = index;
            }

            public ComponentType<?> getKey() {
                return keyUniverse[index];
            }

            public Component getValue() {
                return vals[index];
            }

            public Component setValue(Component value) {
                return IndexedComponentContainer.this.put(getKey(), value);
            }

            public boolean equals(Object o) {
                if (index < 0)
                    return o == this;

                if (!(o instanceof Map.Entry))
                    return false;

                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                Component ourValue = vals[index];
                Object hisValue = e.getValue();
                return (e.getKey() == keyUniverse[index] && Objects.equals(ourValue, hisValue));
            }

            public int hashCode() {
                return entryHashCode(index);
            }

            public String toString() {
                return keyUniverse[index] + "=" + vals[index];
            }

        }

    }

    private int entryHashCode(int index) {
        return (keyUniverse[index].hashCode() ^ vals[index].hashCode());
    }



}
