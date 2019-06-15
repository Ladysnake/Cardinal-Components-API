package nerdhub.cardinal.components.util;

import com.google.common.base.Preconditions;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;
import java.util.Objects;

/**
 * An efficient container for attached {@link Component}.
 * @implNote The implementation is based on {@link java.util.EnumMap} and offers constant time
 * execution for all operations.
 */
public class ComponentContainer {
    /**
     * All of the component types registered at the time
     * this container was created.  (Cached for performance.)
     */
    private ComponentType<?>[] keyUniverse;

    /**
     * Array representation of this map.  The ith element is the value
     * to which universe[i] is currently mapped, or null if it isn't
     * mapped to anything.
     */
    private Component[] vals;

    /**
     * The number of mappings in this container.
     */
    private int size = 0;

    public ComponentContainer() {
        this.keyUniverse = ComponentRegistry.stream().toArray(ComponentType[]::new);
        this.vals = new Component[keyUniverse.length];
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return size;
    }

    public boolean containsKey(ComponentType<?> type) {
        return vals[type.getRawId()] != null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T get(ComponentType<T> key) {
        return (T) vals[key.getRawId()];
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     *
     * @return the previous value associated with specified key, or
     *     <tt>null</tt> if there was no mapping for key.  (A <tt>null</tt>
     *     return can also indicate that the map previously associated
     *     <tt>null</tt> with the specified key.)
     * @throws NullPointerException if the specified key or value is null
     */
    public <V extends Component> V put(ComponentType<V> key, V value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        Preconditions.checkArgument(key.getComponentClass().isInstance(value));
        int index = key.getRawId();
        V oldValue = key.getComponentClass().cast(vals[index]);
        vals[index] = value;
        if (oldValue == null) {
            size++;
        }
        return oldValue;
    }

    public void clear() {
        Arrays.fill(vals, null);
        size = 0;
    }

    public void fromTag(CompoundTag serialized) {
        // TODO
    }

    public CompoundTag toTag(CompoundTag tag) {
        // TODO
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentContainer that = (ComponentContainer) o;
        return this.size == that.size &&
                Arrays.equals(this.vals, that.vals);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(size);
        result = 31 * result + Arrays.hashCode(vals);
        return result;
    }
}
