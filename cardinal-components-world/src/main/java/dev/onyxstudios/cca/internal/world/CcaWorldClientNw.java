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
package dev.onyxstudios.cca.internal.world;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public final class CcaWorldClientNw {
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(ComponentsWorldNetworking.PACKET_ID, (client, handler, buf, res) -> {
            try {
                Identifier componentTypeId = buf.readIdentifier();
                ComponentKey<?> componentType = ComponentRegistry.get(componentTypeId);

                if (componentType == null) {
                    return;
                }

                buf.retain();

                client.execute(() -> {
                    try {
                        assert client.world != null;
                        Component c = componentType.get(client.world);
                        if (c instanceof AutoSyncedComponent) {
                            ((AutoSyncedComponent) c).applySyncPacket(buf);
                        }
                    } finally {
                        buf.release();
                    }
                });
            } catch (Exception e) {
                ComponentsInternals.LOGGER.error("Error while reading world components from network", e);
                throw e;
            }
        });
    }
}
