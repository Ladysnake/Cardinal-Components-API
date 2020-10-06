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
package dev.onyxstudios.cca.internal.level;

import dev.onyxstudios.cca.api.v3.component.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import nerdhub.cardinal.components.api.event.WorldSyncCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

public final class ComponentsLevelNetworking {
    /**
     * {@link CustomPayloadS2CPacket} channel for default level component synchronization.
     *
     * <p> Packets emitted on this channel must begin with the
     * {@link ComponentKey#getId() component's type} (as an Identifier).
     *
     * <p> Components synchronized through this channel will have {@linkplain AutoSyncedComponent#applySyncPacket(PacketByteBuf)}
     * called on the game thread.
     */
    public static final Identifier PACKET_ID = new Identifier("cardinal-components", "level_sync");

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            if (FabricLoader.getInstance().isModLoaded("cardinal-components-world")) {
                WorldSyncCallback.EVENT.register((player, world) -> {
                    ComponentProvider provider = ComponentProvider.fromLevel(world.getLevelProperties());

                    for (ComponentKey<?> key : provider.getComponentContainer().keys()) {
                        // TODO implement relevant methods on LevelProperties
                        key.syncWith(player, provider);
                    }
                });
            }
        }
    }

    // Safe to put in the same class as no client-only class is directly referenced
    public static void initClient() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, (context, buffer) -> {
                try {
                    Identifier componentTypeId = buffer.readIdentifier();
                    ComponentKey<?> componentKey = ComponentRegistry.get(componentTypeId);

                    if (componentKey == null) {
                        return;
                    }

                    PacketByteBuf copy = new PacketByteBuf(buffer.copy());
                    context.getTaskQueue().execute(() -> {
                        try {
                            assert MinecraftClient.getInstance().world != null;
                            Component c = componentKey.get(MinecraftClient.getInstance().world.getLevelProperties());

                            if (c instanceof AutoSyncedComponent) {
                                ((AutoSyncedComponent) c).applySyncPacket(copy);
                            }
                        } finally {
                            copy.release();
                        }
                    });
                } catch (Exception e) {
                    ComponentsInternals.LOGGER.error("Error while reading world save components from network", e);
                    throw e;
                }
            });
        }
    }
}
