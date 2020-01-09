/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 GlassPane
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
package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.event.ChunkSyncCallback;
import nerdhub.cardinal.components.api.util.Components;
import nerdhub.cardinal.components.api.util.sync.ChunkSyncedComponent;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.chunk.Chunk;

import java.util.function.Consumer;

public final class ComponentsChunkNetworking {
    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            ChunkSyncCallback.EVENT.register((player, tracked) -> {
                Components.forEach(ComponentProvider.fromChunk(tracked), (componentType, component) -> {
                    if (component instanceof SyncedComponent) {
                        ((SyncedComponent) component).syncWith(player);
                    }
                });
            });
        }
    }

    // Safe to put in the same class as no client-only class is directly referenced
    public static void initClient() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            ClientSidePacketRegistry.INSTANCE.register(ChunkSyncedComponent.PACKET_ID, (context, buffer) -> {
                int chunkX = buffer.readInt();
                int chunkZ = buffer.readInt();
                Identifier componentTypeId = buffer.readIdentifier();
                ComponentType<?> componentType = ComponentRegistry.INSTANCE.get(componentTypeId);
                if (componentType == null) {
                    return;
                }
                PacketByteBuf copy = new PacketByteBuf(buffer.copy());
                Consumer<Chunk> chunkSync = componentType.asComponentPath()
                        .compose(ComponentProvider::fromChunk)
                        .thenCastTo(SyncedComponent.class)
                        .andThenDo(component -> component.processPacket(context, copy));
                context.getTaskQueue().execute(() -> {
                    // On the client, unloaded chunks return EmptyChunk
                    Chunk chunk = context.getPlayer().world.getChunk(chunkX, chunkZ);
                    chunkSync.accept(chunk);
                    copy.release();
                });
            });
        }
    }
}
