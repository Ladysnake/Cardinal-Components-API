package nerdhub.cardinal.components.api.util.component.container;

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
public final class FastComponentContainer extends AbstractComponentContainer {
    /**
     * All of the component types that can be stored in this container.
     * (Cached for performance.)
     */
    private ComponentType<?>[] keyUniverse = SharedComponentSecrets.getRegisteredComponents();

    private final Int2ObjectOpenHashMap<Component> vals;

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
    public Component put(ComponentType<?> key, Component value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        Preconditions.checkArgument(key.getComponentClass().isInstance(value), value + " is not of type " + key);
        return key.getComponentClass().cast(this.vals.put(key.getRawId(), value));
    }

    @Override
    public void forEach(BiConsumer<? super ComponentType<?>, ? super Component> action) {
        this.vals.int2ObjectEntrySet().fastForEach((e) ->
            action.accept(this.keyUniverse[e.getIntKey()], e.getValue()));
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

    private class Values extends AbstractCollection<Component> {
        public Iterator<Component> iterator() {
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

    private class ValueIterator implements Iterator<Component> {
        ObjectIterator<Component> it = vals.values().iterator();

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        public Component next() {
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
            return keyUniverse[it.nextInt()];
        }

    }

    private class EntryIterator implements Iterator<Entry<ComponentType<?>, Component>> {
        ObjectIterator<Int2ObjectMap.Entry<Component>> it = vals.int2ObjectEntrySet().fastIterator();
        public Entry next() {
            Int2ObjectMap.Entry<Component> entry = it.next();
            return new Entry(entry.getIntKey(), entry.getValue());
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        private class Entry implements Map.Entry<ComponentType<?>, Component> {
            private int rawId;
            private Component value;

            private Entry(int rawId, Component value) {
                this.rawId = rawId;
                this.value = value;
            }

            public ComponentType<?> getKey() {
                return keyUniverse[rawId];
            }

            public Component getValue() {
                return value;
            }

            @Nullable
            public Component setValue(Component value) {
                return FastComponentContainer.this.put(getKey(), value);
            }

        }

    }

}
