package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentType;

public interface ComponentContainer {
    boolean containsKey(ComponentType<?> type);

    /**
     * Returns the value to which the specified component type is mapped,
     * or {@code null} if this container contains no component of that type.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key == k)},
     * then this method returns {@code v}; otherwise it returns
     * {@code null}.  (There can be at most one such mapping.)
     *
     * @param key a registered component type
     * @param <T> the class of the component
     * @return a component of that type, of {@code null} if none has been attached
     */
    <T extends Component> T get(ComponentType<T> key);

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
    <V extends Component> V put(ComponentType<V> key, V value);
}
