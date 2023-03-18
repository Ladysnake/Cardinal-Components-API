/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 OnyxStudios
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
package dev.onyxstudios.cca.api.v3.component;

import dev.onyxstudios.cca.api.v3.component.sync.ComponentPacketWriter;
import dev.onyxstudios.cca.api.v3.component.sync.PlayerSyncPredicate;

import java.util.NoSuchElementException;

/**
 * Interface injected into classes that support the Cardinal Components API.
 *
 * <p>Implementations of this interface should do so through {@link ComponentProvider}.
 *
 * @see ComponentProvider
 * @since 5.0.0
 */
// Note: no method in this interface can be abstract, as it is injected into commonly inherited types by Loom
public interface ComponentAccess {
    /**
     * @param key the key object for the type of component to retrieve
     * @return the nonnull value of the held component of this type
     * @param <C> the type of component to retrieve
     * @throws NullPointerException if {@code key} is null
     * @throws NoSuchElementException if this provider does not provide the desired type of component
     */
    default <C extends Component> C getComponent(ComponentKey<C> key) {
        return key.get(this.asComponentProvider());
    }

    /**
     * Attempts to synchronize the component of the desired type with watching clients.
     *
     * <p>This method has no visible effect if {@linkplain #asComponentProvider() this provider} does not support synchronization, or
     * the associated component does not implement an adequate {@linkplain dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent synchronization interface}.
     *
     * @param key the key object for the type of component to synchronize
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see ComponentKey#sync(Object)
     */
    default void syncComponent(ComponentKey<?> key) {
        key.sync(this.asComponentProvider());
    }

    /**
     * Attempts to synchronize the component of the desired type with watching clients using the specified {@code packetWriter}.
     *
     * <p>This method has no visible effect if {@linkplain #asComponentProvider() this provider} does not support synchronization, or
     * the associated component does not implement an adequate {@linkplain dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent synchronization interface}.
     *
     * @param key the key object for the type of component to synchronize
     * @param packetWriter a writer for the sync packet
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see ComponentKey#sync(Object, ComponentPacketWriter)
     */
    default void syncComponent(ComponentKey<?> key, ComponentPacketWriter packetWriter) {
        key.sync(this.asComponentProvider(), packetWriter);
    }

    /**
     * Attempts to synchronize the component of the desired type with watching clients matching the {@code predicate},
     * using the specified {@code packetWriter}.
     *
     * <p>This method has no visible effect if {@linkplain #asComponentProvider() this provider} does not support synchronization, or
     * the associated component does not implement an adequate {@linkplain dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent synchronization interface}.
     *
     * @param key the key object for the type of component to synchronize
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see ComponentKey#sync(Object, ComponentPacketWriter, PlayerSyncPredicate)
     */
    default void syncComponent(ComponentKey<?> key, ComponentPacketWriter packetWriter, PlayerSyncPredicate predicate) {
        key.sync(this.asComponentProvider(), packetWriter, predicate);
    }

    default ComponentProvider asComponentProvider() {
        return (ComponentProvider) this;
    }
}
