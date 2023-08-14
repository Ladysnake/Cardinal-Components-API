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
package dev.onyxstudios.cca.internal.chunk;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class CcaChunkClient {
    public static void initClient() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-api-v1")) {
            ClientPlayNetworking.registerGlobalReceiver(CardinalComponentsChunk.PACKET_ID, (client, handler, buffer, res) -> {
                try {
                    int chunkX = buffer.readInt();
                    int chunkZ = buffer.readInt();
                    Identifier componentTypeId = buffer.readIdentifier();
                    ComponentKey<?> componentType = ComponentRegistry.get(componentTypeId);
                    if (componentType == null) {
                        return;
                    }
                    buffer.retain();
                    client.execute(() -> {
                        try {
                            // Note: on the client, unloaded chunks return EmptyChunk
                            componentType.maybeGet(Objects.requireNonNull(client.world).getChunk(chunkX, chunkZ))
                                .filter(c -> c instanceof AutoSyncedComponent)
                                .ifPresent(c -> ((AutoSyncedComponent) c).applySyncPacket(buffer));
                        } finally {
                            buffer.release();
                        }
                    });
                } catch (Exception e) {
                    ComponentsInternals.LOGGER.error("Error while reading chunk components from network", e);
                    throw e;
                }
            });
        }
        if (FabricLoader.getInstance().isModLoaded("fabric-lifecycle-events-v1")) {
            ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> ((ComponentProvider) chunk).getComponentContainer().onServerLoad());
            ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> ((ComponentProvider) chunk).getComponentContainer().onServerUnload());
        }
    }
}
