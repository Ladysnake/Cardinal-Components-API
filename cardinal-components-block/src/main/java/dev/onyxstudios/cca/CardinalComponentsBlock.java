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
package dev.onyxstudios.cca;

import dev.onyxstudios.cca.api.component.v3.util.BlockEntitySyncedComponent;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;

public class CardinalComponentsBlock {
    // Safe to put in the same class as no client-only class is directly referenced
    public static void initClient() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            ClientSidePacketRegistry.INSTANCE.register(BlockEntitySyncedComponent.PACKET_ID, (context, buffer) -> {
                try {
                    BlockPos position = buffer.readBlockPos();
                    Identifier componentTypeId = buffer.readIdentifier();
                    ComponentType<?> componentType = ComponentRegistry.INSTANCE.get(componentTypeId);
                    if (componentType == null) {
                        return;
                    }
                    PacketByteBuf copy = new PacketByteBuf(buffer.copy());
                    context.getTaskQueue().execute(() -> {
                        try {
                            componentType.maybeGet(context.getPlayer().world.getBlockEntity(position))
                                .filter(c -> c instanceof SyncedComponent)
                                .ifPresent(c -> ((SyncedComponent) c).processPacket(context, copy));
                        } finally {
                            copy.release();
                        }
                    });
                } catch (Exception e) {
                    ComponentsInternals.LOGGER.error("Error while reading block entity components from network", e);
                    throw e;
                }
            });
        }
    }
}
