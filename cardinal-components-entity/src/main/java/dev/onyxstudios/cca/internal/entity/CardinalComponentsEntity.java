/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.cca.internal.entity;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerCopyCallback;
import dev.onyxstudios.cca.api.v3.entity.PlayerSyncCallback;
import dev.onyxstudios.cca.api.v3.entity.TrackingStartCallback;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

import java.util.Set;

public final class CardinalComponentsEntity {
    /**
     * {@link CustomPayloadS2CPacket} channel for default entity component synchronization.
     *
     * <p> Packets emitted on this channel must begin with, in order, the {@link Entity#getEntityId() entity id} (as an int),
     * and the {@link ComponentKey#getId() component's type} (as an Identifier).
     *
     * <p> Components synchronized through this channel will have {@linkplain AutoSyncedComponent#applySyncPacket(PacketByteBuf)}
     * called on the game thread.
     */
    public static final Identifier PACKET_ID = new Identifier("cardinal-components", "entity_sync");

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            PlayerSyncCallback.EVENT.register(player -> syncEntityComponents(player, player));
            TrackingStartCallback.EVENT.register(CardinalComponentsEntity::syncEntityComponents);
        }
        PlayerCopyCallback.EVENT.register(CardinalComponentsEntity::copyData);
    }

    private static void copyData(ServerPlayerEntity original, ServerPlayerEntity clone, boolean lossless) {
        boolean keepInventory = original.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || clone.isSpectator();
        Set<ComponentKey<?>> keys = ((ComponentProvider) original).getComponentContainer().keys();

        for (ComponentKey<?> key : keys) {
            copyData(original, clone, lossless, keepInventory, key);
        }
    }

    private static <C extends Component> void copyData(ServerPlayerEntity original, ServerPlayerEntity clone, boolean lossless, boolean keepInventory, ComponentKey<C> key) {
        C from = key.get(original);
        C to = key.get(clone);
        CardinalEntityInternals.getRespawnCopyStrategy(key).copyForRespawn(from, to, lossless, keepInventory);
    }

    private static void syncEntityComponents(ServerPlayerEntity player, Entity tracked) {
        ComponentProvider provider = (ComponentProvider) tracked;

        for (ComponentKey<?> key : provider.getComponentContainer().keys()) {
            key.syncWith(player, provider);
        }
    }

    // Safe to put in the same class as no client-only class is directly referenced
    public static void initClient() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, (context, buffer) -> {
                try {
                    int entityId = buffer.readInt();
                    Identifier componentTypeId = buffer.readIdentifier();
                    ComponentKey<?> componentType = ComponentRegistry.get(componentTypeId);
                    if (componentType == null) {
                        return;
                    }
                    PacketByteBuf copy = new PacketByteBuf(buffer.copy());
                    context.getTaskQueue().execute(() -> {
                        try {
                            componentType.maybeGet(context.getPlayer().world.getEntityById(entityId))
                                .filter(c -> c instanceof AutoSyncedComponent)
                                .ifPresent(c -> ((AutoSyncedComponent) c).applySyncPacket(copy));
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
