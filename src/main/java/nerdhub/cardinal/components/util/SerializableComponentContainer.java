package nerdhub.cardinal.components.util;

import com.google.common.base.Preconditions;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;
import java.util.Objects;

/**
 * An efficient container for attached {@link Component}.
 *
 * @implNote The implementation is based on {@link java.util.EnumMap} and offers constant time
 * execution for all operations.
 */
public final class SerializableComponentContainer implements ComponentContainer {
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

    public SerializableComponentContainer() {
        this.keyUniverse = null;
        this.universeSize = 0;
        this.vals = null;
        this.size = 0;
    }

    /**
     * Returns the number of components held by this container.
     *
     * @return the number of components in this container
     */
    public int size() {
        return size;
    }

    @Override
    public boolean containsKey(ComponentType<?> type) {
        final int rawId = type.getRawId();
        return rawId < universeSize && vals[rawId] != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> T get(ComponentType<T> key) {
        final int rawId = key.getRawId();
        return rawId < universeSize ? (T) vals[rawId] : null;
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
        if (index >= universeSize) {
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

    /**
     * Returns a string representation of this container. The string representation
     * consists of a list of key-value mappings in the order of type registration,
     * enclosed in braces (<tt>"{}"</tt>). Adjacent mappings are separated by the
     * characters <tt>", "</tt> (comma and space). Each key-value mapping is rendered
     * as the key followed by an equals sign (<tt>"="</tt>) followed by the
     * associated value.  Keys and values are converted to strings as by
     * {@link String#valueOf(Object)}.
     *
     * @return a string representation of this container
     */
    public String toString() {
        if (this.size == 0) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int i = 0;;) {
            ComponentType<?> key = this.keyUniverse[i];
            Component value = this.vals[i];
            if (value != null) {
                sb.append(key);
                sb.append('=');
                sb.append(value);
            }
            if (++i == this.universeSize) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializableComponentContainer that = (SerializableComponentContainer) o;
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
