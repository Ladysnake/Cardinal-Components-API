/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
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
import nerdhub.cardinal.components.api.event.PlayerSyncCallback;
import nerdhub.cardinal.components.api.event.TrackingStartCallback;
import nerdhub.cardinal.components.api.util.Components;
import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.function.Consumer;

public final class ComponentsEntityNetworking {
    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            PlayerSyncCallback.EVENT.register(player -> syncEntityComponents(player, player));
            TrackingStartCallback.EVENT.register(ComponentsEntityNetworking::syncEntityComponents);
        }
    }

    private static void syncEntityComponents(ServerPlayerEntity player, Entity tracked) {
        Components.forEach(ComponentProvider.fromEntity(tracked), (componentType, component) -> {
            if (component instanceof SyncedComponent) {
                ((SyncedComponent) component).syncWith(player);
            }
        });
    }

    // Safe to put in the same class as no client-only class is directly referenced
    public static void initClient() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-v0")) {
            ClientSidePacketRegistry.INSTANCE.register(EntitySyncedComponent.PACKET_ID, (context, buffer) -> {
                int entityId = buffer.readInt();
                Identifier componentTypeId = buffer.readIdentifier();
                ComponentType<?> componentType = ComponentRegistry.INSTANCE.get(componentTypeId);
                if (componentType == null) {
                    return;
                }
                PacketByteBuf copy = new PacketByteBuf(buffer.copy());
                // Functional null avoidance with off-thread lambda instantiation
                Consumer<Entity> entitySync = componentType.asComponentPath()
                        .compose(ComponentProvider::fromEntity)
                        .thenCastTo(SyncedComponent.class)
                        .andThenDo(component -> component.processPacket(context, copy));
                context.getTaskQueue().execute(() -> {
                    Entity entity = context.getPlayer().world.getEntityById(entityId);
                    entitySync.accept(entity);
                    copy.release();
                });
            });
        }
    }
}
