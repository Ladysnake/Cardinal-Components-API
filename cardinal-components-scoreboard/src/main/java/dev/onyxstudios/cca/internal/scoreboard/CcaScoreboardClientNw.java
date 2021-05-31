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
package dev.onyxstudios.cca.internal.scoreboard;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class CcaScoreboardClientNw {
    public static void initClient() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-api-v1")) {
            registerScoreboardSync(ComponentsScoreboardNetworking.TEAM_PACKET_ID, buf -> {
                String teamName = buf.readString();
                return (componentType, scoreboard) -> componentType.maybeGet(scoreboard.getTeam(teamName));
            });
            registerScoreboardSync(ComponentsScoreboardNetworking.SCOREBOARD_PACKET_ID,
                buf -> ComponentKey::maybeGet
            );
        }
    }

    private static void registerScoreboardSync(Identifier packetId, Function<PacketByteBuf, BiFunction<ComponentKey<?>, Scoreboard, Optional<? extends Component>>> reader) {
        ClientPlayNetworking.registerGlobalReceiver(packetId, (client, handler, buffer, res) -> {
            try {
                BiFunction<ComponentKey<?>, Scoreboard, Optional<? extends Component>> getter = reader.apply(buffer);
                Identifier componentTypeId = buffer.readIdentifier();
                ComponentKey<?> componentType = ComponentRegistry.get(componentTypeId);

                if (componentType != null) {
                    buffer.retain();
                    client.execute(() -> {
                        try {
                            getter.apply(componentType, handler.getWorld().getScoreboard())
                                .filter(c -> c instanceof AutoSyncedComponent)
                                .ifPresent(c -> ((AutoSyncedComponent) c).applySyncPacket(buffer));
                        } finally {
                            buffer.release();
                        }
                    });
                }
            } catch (Exception e) {
                ComponentsInternals.LOGGER.error("Error while reading scoreboard components from network", e);
                throw e;
            }
        });
    }
}
