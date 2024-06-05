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
package org.ladysnake.cca.internal;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import org.ladysnake.cca.api.v3.block.BlockEntitySyncAroundCallback;
import org.ladysnake.cca.api.v3.block.BlockEntitySyncCallback;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.block.StaticBlockComponentPlugin;

public class CardinalComponentsBlock {

    /**
     * {@link CustomPayloadS2CPacket} channel for default block entity component synchronization.
     *
     * <p> Components synchronized through this channel will have {@linkplain AutoSyncedComponent#applySyncPacket(net.minecraft.network.RegistryByteBuf)}
     * called on the game thread.
     */
    public static final CustomPayload.Id<ComponentUpdatePayload<BlockEntityAddress>> PACKET_ID = ComponentUpdatePayload.id("block_entity_sync");

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-api-v1")) {
            ComponentUpdatePayload.register(PACKET_ID, BlockEntityAddress.CODEC);
            BlockEntitySyncCallback.EVENT.register((player, tracked) -> {
                ComponentProvider provider = (ComponentProvider) tracked;

                for (ComponentKey<?> key : provider.getComponentContainer().keys()) {
                    key.syncWith(player, provider);
                }
            });
            BlockEntitySyncAroundCallback.EVENT.register(tracked -> {
                for (ComponentKey<?> key : ((ComponentProvider) tracked).getComponentContainer().keys()) {
                    tracked.syncComponent(key);
                }
            });
        }
        if (FabricLoader.getInstance().isModLoaded("fabric-lifecycle-events-v1")) {
            ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, world) -> ((ComponentProvider) be).getComponentContainer().onServerLoad());
            ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, world) -> ((ComponentProvider) be).getComponentContainer().onServerUnload());
        }
        StaticBlockComponentPlugin.INSTANCE.ensureInitialized();
    }
}
