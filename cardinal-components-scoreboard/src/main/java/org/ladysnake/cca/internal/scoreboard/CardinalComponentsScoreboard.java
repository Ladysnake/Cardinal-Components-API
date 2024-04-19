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
package org.ladysnake.cca.internal.scoreboard;

import com.mojang.datafixers.util.Unit;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.scoreboard.Team;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardSyncCallback;
import org.ladysnake.cca.api.v3.scoreboard.TeamAddCallback;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.base.MorePacketCodecs;

public final class CardinalComponentsScoreboard {
    /**
     * {@link CustomPayloadS2CPacket} channel for default scoreboard component synchronization.
     *
     * <p> Components synchronized through this channel will have {@linkplain AutoSyncedComponent#applySyncPacket(net.minecraft.network.RegistryByteBuf)}
     * called on the game thread.
     */
    public static final CustomPayload.Id<ComponentUpdatePayload<Unit>> SCOREBOARD_PACKET_ID = CustomPayload.id("cardinal-components:scoreboard_sync");
    /**
     * {@link CustomPayloadS2CPacket} channel for default team component synchronization.
     *
     * <p> Components synchronized through this channel will have {@linkplain AutoSyncedComponent#applySyncPacket(net.minecraft.network.RegistryByteBuf)}
     * called on the game thread.
     */
    public static final CustomPayload.Id<ComponentUpdatePayload<String>> TEAM_PACKET_ID = CustomPayload.id("cardinal-components:team_sync");

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-api-v1")) {
            ComponentUpdatePayload.register(SCOREBOARD_PACKET_ID, MorePacketCodecs.EMPTY);
            ComponentUpdatePayload.register(TEAM_PACKET_ID, PacketCodecs.STRING);
            ScoreboardSyncCallback.EVENT.register((player, tracked) -> {
                for (ComponentKey<?> key : tracked.asComponentProvider().getComponentContainer().keys()) {
                    key.syncWith(player, tracked.asComponentProvider());
                }

                for (Team team : tracked.getTeams()) {
                    for (ComponentKey<?> key : team.asComponentProvider().getComponentContainer().keys()) {
                        key.syncWith(player, team.asComponentProvider());
                    }
                }
            });
            TeamAddCallback.EVENT.register((tracked) -> {
                for (ComponentKey<?> key : tracked.asComponentProvider().getComponentContainer().keys()) {
                    tracked.syncComponent(key);
                }
            });
        }
        if (FabricLoader.getInstance().isModLoaded("fabric-lifecycle-events-v1")) {
            ServerLifecycleEvents.SERVER_STARTED.register((server) -> ((ComponentProvider) server.getScoreboard()).getComponentContainer().onServerLoad());
            ServerLifecycleEvents.SERVER_STOPPED.register((server) -> ((ComponentProvider) server.getScoreboard()).getComponentContainer().onServerUnload());
        }
    }
}
