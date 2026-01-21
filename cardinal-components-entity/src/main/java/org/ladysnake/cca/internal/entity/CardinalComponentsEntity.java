/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2026 Ladysnake
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
package org.ladysnake.cca.internal.entity;

import com.mojang.datafixers.util.Unit;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.ConversionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gamerules.GameRules;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.entity.C2SSelfMessagingComponent;
import org.ladysnake.cca.api.v3.entity.PlayerSyncCallback;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.entity.TrackingStartCallback;
import org.ladysnake.cca.api.v8.component.CardinalComponent;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.base.ComponentsInternals;
import org.ladysnake.cca.internal.base.MorePacketCodecs;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class CardinalComponentsEntity {
    /**
     * {@link ClientboundCustomPayloadPacket} channel for default entity component synchronization.
     *
     * <p> Components synchronized through this channel will have {@linkplain AutoSyncedComponent#applySyncPacket(net.minecraft.network.RegistryFriendlyByteBuf)}
     * called on the game thread.
     */
    public static final CustomPacketPayload.Type<ComponentUpdatePayload<Integer>> PACKET_ID = ComponentUpdatePayload.id("entity_sync");
    /**
     * {@link net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket} channel for C2S player component messages.
     *
     * <p> Packets emitted on this channel must begin with the {@link ComponentKey#getId() component's type} (as an Identifier).
     *
     * <p> Components synchronized through this channel will have {@linkplain org.ladysnake.cca.api.v3.entity.C2SSelfMessagingComponent#handleC2SMessage(net.minecraft.network.RegistryFriendlyByteBuf)}
     * called on the game thread.
     */
    public static final CustomPacketPayload.Type<ComponentUpdatePayload<Unit>> C2S_SELF_PACKET_ID = ComponentUpdatePayload.id("player_message_c2s");
    private static final Set<Identifier> unknownC2SPlayerComponents = new HashSet<>();

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-api-v1")) {
            ComponentUpdatePayload.register(PACKET_ID, ByteBufCodecs.VAR_INT);
            PayloadTypeRegistry.serverboundPlay().register(C2S_SELF_PACKET_ID, ComponentUpdatePayload.codec(C2S_SELF_PACKET_ID, MorePacketCodecs.EMPTY));
            PlayerSyncCallback.EVENT.register(player -> syncEntityComponents(player, player));
            TrackingStartCallback.EVENT.register(CardinalComponentsEntity::syncEntityComponents);
            ServerPlayNetworking.registerGlobalReceiver(CardinalComponentsEntity.C2S_SELF_PACKET_ID, (payload, ctx) -> {
                try {
                    Optional<ComponentKey<?>> componentKey = payload.componentKey();
                    if (componentKey.isPresent()) {
                        if (componentKey.get().getNullable(ctx.player()) instanceof C2SSelfMessagingComponent synced) {
                            synced.handleC2SMessage(payload.buf());
                        } else if (payload.required() && (unknownC2SPlayerComponents.add(payload.componentKeyId()) || FabricLoader.getInstance().isDevelopmentEnvironment())) {
                            // In prod, only log the first time an unknown component update is received
                            ComponentsInternals.LOGGER.warn("[Cardinal Components API] Received an update for component {} from player {}, but this component is not registered for player entities", payload.componentKeyId(), ctx.player());
                        }
                    } else if (payload.required() && (unknownC2SPlayerComponents.add(payload.componentKeyId()) || FabricLoader.getInstance().isDevelopmentEnvironment())) {
                        ComponentsInternals.LOGGER.warn("[Cardinal Components API] Received an update for unknown component {} from player {}", payload.componentKeyId(), ctx.player());
                    }
                } finally {
                    payload.buf().release();
                }
            });
        }
        if (FabricLoader.getInstance().isModLoaded("fabric-lifecycle-events-v1")) {
            ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> ((ComponentProvider) entity).getComponentContainer().onServerLoad());
            ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> ((ComponentProvider) entity).getComponentContainer().onServerUnload());
        }
        ServerLivingEntityEvents.MOB_CONVERSION.register(RespawnCopyStrategy.EVENT_PHASE, CardinalComponentsEntity::copyData);
        ServerPlayerEvents.COPY_FROM.register(RespawnCopyStrategy.EVENT_PHASE, CardinalComponentsEntity::copyData);
        StaticEntityComponentPlugin.INSTANCE.ensureInitialized();
    }

    private static void copyData(LivingEntity original, LivingEntity clone, ConversionParams context) {
        Set<ComponentKey<?>> keys = ((ComponentProvider) original).getComponentContainer().keys();

        for (ComponentKey<?> key : keys) {
            if (key.isProvidedBy(clone)) {
                copyData(original, clone, original.registryAccess(), false, context.keepEquipment(), context.type() == ConversionType.SINGLE, key);
            }
        }
    }

    private static void copyData(ServerPlayer original, ServerPlayer clone, boolean lossless) {
        boolean keepInventory = original.level().getGameRules().get(GameRules.KEEP_INVENTORY) || clone.isSpectator();
        Set<ComponentKey<?>> keys = ((ComponentProvider) original).getComponentContainer().keys();

        for (ComponentKey<?> key : keys) {
            copyData(original, clone, original.registryAccess(), lossless, keepInventory, !((SwitchablePlayerEntity) original).cca$isSwitchingCharacter(), key);
        }
    }

    private static <C extends CardinalComponent> void copyData(LivingEntity original, LivingEntity clone, HolderLookup.Provider registryLookup, boolean lossless, boolean keepInventory, boolean sameCharacter, ComponentKey<C> key) {
        C from = key.get(original);
        C to = key.get(clone);
        RespawnCopyStrategy.get(key, original.getClass()).copyForRespawn(from, to, registryLookup, lossless, keepInventory, sameCharacter);
    }

    private static void syncEntityComponents(ServerPlayer player, Entity tracked) {
        ComponentProvider provider = (ComponentProvider) tracked;

        for (ComponentKey<?> key : provider.getComponentContainer().keys()) {
            key.syncWith(player, provider);
        }
    }
}
