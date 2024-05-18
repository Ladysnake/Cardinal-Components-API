/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 Ladysnake
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
package org.ladysnake.cca.internal.chunk;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;
import org.ladysnake.cca.api.v3.chunk.ChunkSyncCallback;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.base.MorePacketCodecs;

public final class CardinalComponentsChunk {
    public static final CustomPayload.Id<ComponentUpdatePayload<ChunkPos>> PACKET_ID = CustomPayload.id("cardinal-components:chunk_sync");

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-api-v1")) {
            ComponentUpdatePayload.register(PACKET_ID, MorePacketCodecs.CHUNKPOS);
            ChunkSyncCallback.EVENT.register((player, tracked) -> {
                for (ComponentKey<?> key : tracked.asComponentProvider().getComponentContainer().keys()) {
                    key.syncWith(player, (ComponentProvider) tracked);
                }
            });
        }
        if (FabricLoader.getInstance().isModLoaded("fabric-lifecycle-events-v1")) {
            ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> ((ComponentProvider) chunk).getComponentContainer().onServerLoad());
            ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> ((ComponentProvider) chunk).getComponentContainer().onServerUnload());
        }
        StaticChunkComponentPlugin.INSTANCE.ensureInitialized();
    }
}
