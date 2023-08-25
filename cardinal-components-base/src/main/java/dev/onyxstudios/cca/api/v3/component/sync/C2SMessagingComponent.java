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
package dev.onyxstudios.cca.api.v3.component.sync;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

/**
 * @see dev.onyxstudios.cca.api.v3.component.ComponentKey#sendToServer(ComponentProvider, C2SComponentPacketWriter)
 */
@ApiStatus.Experimental
public interface C2SMessagingComponent extends Component {
    /**
     * Checks whether the client associated with {@code sender} is habilitated to perform an update on this component
     *
     * <p>Mods should make sure all preconditions are considered when implementing this method - e.g. "is the player in range"
     * and/or "is the player an operator" and/or "does the player have the right menu opened"
     *
     * @param sender the player object associated with the client emitting the message
     * @return {@code true} if the component can proceed with a sync from {@code sender}, {@code false} otherwise
     */
    boolean mayHandleMessageFromClient(ServerPlayerEntity sender);

    /**
     * Handles a message sent through this component's channel by {@code sender}
     */
    void handleMessageFromClient(ServerPlayerEntity sender, PacketByteBuf buf);
}
