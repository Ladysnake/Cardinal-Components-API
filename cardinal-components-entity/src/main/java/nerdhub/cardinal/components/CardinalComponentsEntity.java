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
package nerdhub.cardinal.components;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.event.PlayerCopyCallback;
import nerdhub.cardinal.components.api.event.PlayerSyncCallback;
import nerdhub.cardinal.components.api.event.TrackingStartCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;
import nerdhub.cardinal.components.internal.ComponentsInternals;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

public final class CardinalComponentsEntity {
    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            PlayerSyncCallback.EVENT.register(player -> syncEntityComponents(player, player));
            TrackingStartCallback.EVENT.register(CardinalComponentsEntity::syncEntityComponents);
        }
        PlayerCopyCallback.EVENT.register(CardinalComponentsEntity::copyData);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyData(ServerPlayerEntity original, ServerPlayerEntity clone, boolean lossless) {
        boolean keepInventory = original.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || clone.isSpectator();
        ComponentProvider.fromEntity(original).forEachComponent((type, from) -> type.maybeGet(clone).ifPresent(
            to -> EntityComponents.getRespawnCopyStrategy((ComponentType) type).copyForRespawn(from, to, lossless, keepInventory)
        ));
    }

    private static void syncEntityComponents(ServerPlayerEntity player, Entity tracked) {
        ComponentProvider.fromEntity(tracked).forEachComponent((componentType, component) -> {
            if (component instanceof SyncedComponent) {
                ((SyncedComponent) component).syncWith(player);
            }
        });
    }

    // Safe to put in the same class as no client-only class is directly referenced
    public static void initClient() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            ClientSidePacketRegistry.INSTANCE.register(EntitySyncedComponent.PACKET_ID, (context, buffer) -> {
                try {
                    int entityId = buffer.readInt();
                    Identifier componentTypeId = buffer.readIdentifier();
                    ComponentType<?> componentType = ComponentRegistry.INSTANCE.get(componentTypeId);
                    if (componentType == null) {
                        return;
                    }
                    PacketByteBuf copy = new PacketByteBuf(buffer.copy());
                    context.getTaskQueue().execute(() -> {
                        try {
                            componentType.maybeGet(context.getPlayer().world.getEntityById(entityId))
                                .filter(c -> c instanceof SyncedComponent)
                                .ifPresent(c -> ((SyncedComponent) c).processPacket(context, copy));
                        } finally {
                            copy.release();
                        }
                    });
                } catch (Exception e) {
                    ComponentsInternals.LOGGER.error("Error while reading entity components from network", e);
                    throw e;
                }
            });
        }
    }
}
