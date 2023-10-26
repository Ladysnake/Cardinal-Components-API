/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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
package org.ladysnake.cca.internal.level;

import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.world.WorldSyncCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.world.WorldProperties;

public final class CardinalComponentsLevel {
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
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-api-v1")) {
            if (FabricLoader.getInstance().isModLoaded("cardinal-components-world")) {
                WorldSyncCallback.EVENT.register((player, world) -> {
                    WorldProperties props = world.getLevelProperties();

                    for (ComponentKey<?> key : props.asComponentProvider().getComponentContainer().keys()) {
                        key.syncWith(player, props.asComponentProvider());
                    }
                });
            }
        }
    }

}
