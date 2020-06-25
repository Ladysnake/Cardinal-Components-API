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
package nerdhub.cardinal.components.api.component.extension;

import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

/**
 * A component that is synchronized with clients observing it.
 *
 * <p> Component providers that support synchronization call
 * {@link #syncWith(ServerPlayerEntity)} on provided {@code SyncedComponent} instances
 * when needed.
 *
 * @see nerdhub.cardinal.components.api.util.sync.BaseSyncedComponent
 */
public interface SyncedComponent extends Component {
    /**
     * Marks this component as dirty.
     *
     * <p> Calling {@code markDirty} effectively suggests to this component
     * that its state should be sent to relevant clients.
     * A component may send an update as soon as this method is called, or it may merely
     * record the change so that an external system performs the synchronization.
     *
     * @deprecated use {@link #sync()} for an alternative with better semantics
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default void markDirty() {
        this.sync();
    }

    /**
     * Synchronizes this component with any watcher.
     *
     * <p> Implementations that wish to keep some property in sync with watching clients
     * should call this method every time that property is updated.
     * Example: <pre>{@code
     *      private int syncedValue;
     *
     *      public void setSyncedValue(int newValue) {
     *          this.syncedValue = newValue;
     *          this.sync();
     *      }
     * }</pre>
     *
     * @since 2.1.0
     */
    void sync();

    /**
     * Immediately synchronizes this component with the given player.
     *
     * <p>
     * This method may be called when a component provider implementation is itself
     * synchronized with {@code player}. A component may ignore a synchronization
     * request if it does not need to send any data to the given {@code player}.
     *
     * @see #processPacket(PacketContext, PacketByteBuf)
     */
    void syncWith(ServerPlayerEntity player);

    /**
     * Process a synchronization packet.
     *
     * <p> This method is usually called on the game thread,
     * although some callers may allow processing on the network thread.
     * Component synchronization plugin channels must document the
     * expected threading behaviour for packet processing.
     *
     * @see #syncWith(ServerPlayerEntity)
     * @see net.fabricmc.fabric.api.network.PacketConsumer
     */
    void processPacket(PacketContext ctx, PacketByteBuf buf);
}
