/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 OnyxStudios
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
package dev.onyxstudios.cca.internal.base;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.packet.CustomPayload;

import java.util.Optional;
import java.util.function.BiFunction;

public final class CcaClientInternals {
    public static <T extends ComponentUpdatePayload<?>> void registerComponentSync(CustomPayload.Id<T> packetId, BiFunction<T, ClientPlayNetworking.Context, Optional<? extends Component>> getter) {
        ClientPlayNetworking.registerGlobalReceiver(packetId, (payload, ctx) -> {
            try {
                getter.apply(payload, ctx).ifPresent(c -> {
                    if (c instanceof AutoSyncedComponent synced) {
                        synced.applySyncPacket(payload.buf());
                    }
                });
            } finally {
                payload.buf().release();
            }
        });
    }

}
