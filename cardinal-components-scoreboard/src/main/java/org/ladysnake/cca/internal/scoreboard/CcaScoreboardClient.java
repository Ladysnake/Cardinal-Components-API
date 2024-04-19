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

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.ladysnake.cca.internal.base.CcaClientInternals;

import java.util.Objects;

public final class CcaScoreboardClient {
    public static void initClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
            if (networkHandler != null) {
                Scoreboard scoreboard = networkHandler.getScoreboard();
                scoreboard.asComponentProvider().getComponentContainer().tickClientComponents();

                for (Team team : scoreboard.getTeams()) {
                    team.asComponentProvider().getComponentContainer().tickClientComponents();
                }
            }
        });
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-api-v1")) {
            CcaClientInternals.registerComponentSync(
                CardinalComponentsScoreboard.TEAM_PACKET_ID,
                (payload, ctx) -> payload.componentKey().flatMap(key -> key.maybeGet(Objects.requireNonNull(ctx.client().world).getScoreboard().getTeam(payload.targetData()))
            ));
            CcaClientInternals.registerComponentSync(
                CardinalComponentsScoreboard.SCOREBOARD_PACKET_ID,
                (payload, ctx) -> payload.componentKey().flatMap(
                    key -> key.maybeGet(Objects.requireNonNull(ctx.client().world).getScoreboard())
                )
            );
        }
    }
}
