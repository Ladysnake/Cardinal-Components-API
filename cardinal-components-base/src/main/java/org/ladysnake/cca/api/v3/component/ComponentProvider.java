/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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
package org.ladysnake.cca.api.v3.component;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.sync.PlayerSyncPredicate;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @see ComponentAccess
 * @see ComponentContainer
 */
public interface ComponentProvider extends ComponentAccess {

    /**
     * @return a runtime-generated component container storing statically declared components.
     */
    ComponentContainer getComponentContainer();

    /**
     * Retrieves an iterator describing the list of players who may receive
     * component sync packets from this provider.
     *
     * <p>Individual components can {@linkplain PlayerSyncPredicate#shouldSyncWith(ServerPlayerEntity) filter
     * which players among the returned list should receive their packets};
     * this method does <em>not</em> take current components into account, so callers should perform
     * additional checks as needed.
     *
     * @return a list of player candidates for receiving component sync packets
     */
    default Iterable<ServerPlayerEntity> getRecipientsForComponentSync() {
        return List.of();
    }

    /**
     * Produces a sync packet using the given information.
     *
     * <p>It is the responsibility of the caller to {@link ByteBuf#release() release} the buffer after this method returns.
     *
     * @param key  the key describing the component being synchronized
     * @param data the component's raw sync data
     * @return a {@link ComponentUpdatePayload} that has all the information required to perform the component sync
     * @since 6.0.0
     */
    @Nullable
    default <C extends AutoSyncedComponent> CustomPayload toComponentPacket(ComponentKey<? super C> key, RegistryByteBuf data) {
        return null;
    }
}
