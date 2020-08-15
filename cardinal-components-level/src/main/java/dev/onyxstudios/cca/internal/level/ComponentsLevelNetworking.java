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

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.event.WorldSyncCallback;
import nerdhub.cardinal.components.api.util.sync.LevelSyncedComponent;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class ComponentsLevelNetworking {
    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            if (FabricLoader.getInstance().isModLoaded("cardinal-components-world")) {
                WorldSyncCallback.EVENT.register((player, world) -> {
                    ComponentContainer<?> container = Objects.requireNonNull(ComponentProvider.fromLevel(world.getLevelProperties()).getComponentContainer());

                    for (ComponentKey<?> key : container.keys()) {
                        Component component = key.getFromContainer(container);
                        if (component instanceof SyncedComponent) {
                            ((SyncedComponent) component).syncWith(player);
                        }
                    }
                });
            }
        }
    }

    // Safe to put in the same class as no client-only class is directly referenced
    public static void initClient() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            ClientSidePacketRegistry.INSTANCE.register(LevelSyncedComponent.PACKET_ID, (context, buffer) -> {
                try {
                    Identifier componentTypeId = buffer.readIdentifier();
                    ComponentType<?> componentType = ComponentRegistry.INSTANCE.get(componentTypeId);
                    if (componentType == null) {
                        return;
                    }
                    PacketByteBuf copy = new PacketByteBuf(buffer.copy());
                    context.getTaskQueue().execute(() -> {
                        try {
                            assert MinecraftClient.getInstance().world != null;
                            Component c = componentType.get(MinecraftClient.getInstance().world.getLevelProperties());
                            if (c instanceof SyncedComponent) {
                                ((SyncedComponent) c).processPacket(context, copy);
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
