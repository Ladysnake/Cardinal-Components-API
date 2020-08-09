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
package dev.onyxstudios.cca.internal;

import dev.onyxstudios.cca.api.v3.block.BlockEntitySyncAroundCallback;
import dev.onyxstudios.cca.api.v3.block.BlockEntitySyncCallback;
import dev.onyxstudios.cca.api.v3.block.BlockEntitySyncedComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import dev.onyxstudios.cca.internal.base.InternalComponentProvider;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("unused") // entrypoint
public class CardinalComponentsBlock {
    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            BlockEntitySyncCallback.EVENT.register((player, tracked) -> {
                for (ComponentKey<?> key : ((InternalComponentProvider) tracked).getComponentContainer().keys()) {
                    Component component = key.getNullable(tracked);

                    if (component instanceof SyncedComponent) {
                        ((SyncedComponent) component).syncWith(player);
                    }
                }
            });
            BlockEntitySyncAroundCallback.EVENT.register(tracked -> {
                for (ComponentKey<?> key : ((InternalComponentProvider) tracked).getComponentContainer().keys()) {
                    Component component = key.getNullable(tracked);

                    if (component instanceof SyncedComponent) {
                        ((SyncedComponent) component).sync();
                    }
                }
            });
        }
    }

    // Safe to put in the same class as no client-only class is directly referenced
    public static void initClient() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            ClientSidePacketRegistry.INSTANCE.register(BlockEntitySyncedComponent.PACKET_ID, (context, buffer) -> {
                try {
                    Identifier blockEntityTypeId = buffer.readIdentifier();
                    BlockPos position = buffer.readBlockPos();
                    Identifier componentTypeId = buffer.readIdentifier();
                    BlockEntityType<?> blockEntityType = Registry.BLOCK_ENTITY_TYPE.get(blockEntityTypeId);
                    ComponentType<?> componentType = ComponentRegistry.INSTANCE.get(componentTypeId);

                    if (componentType == null || blockEntityType == null) {
                        return;
                    }

                    PacketByteBuf copy = new PacketByteBuf(buffer.copy());
                    context.getTaskQueue().execute(() -> {
                        try {
                            componentType.maybeGet(blockEntityType.get(context.getPlayer().world, position))
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
