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
package org.ladysnake.cca.internal.level;

import com.mojang.datafixers.util.Unit;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v8.level.LevelSyncCallback;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.base.MorePacketCodecs;

import java.util.IdentityHashMap;
import java.util.Map;

public final class CardinalComponentsLevel {
    public static final CustomPacketPayload.Type<ComponentUpdatePayload<Unit>> PACKET_ID = ComponentUpdatePayload.id("world_sync");
    private static final Map<@Nullable ResourceKey<Level>, ComponentContainer.Factory<Level>> levelContainerFactories = new IdentityHashMap<>();

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("fabric-networking-api-v1")) {
            ComponentUpdatePayload.register(PACKET_ID, MorePacketCodecs.EMPTY);
            LevelSyncCallback.EVENT.register((player, world) -> {
                ComponentProvider provider = (ComponentProvider) world;
                for (ComponentKey<?> key : provider.getComponentContainer().keys()) {
                    key.syncWith(player, provider);
                }
            });
        }
        if (FabricLoader.getInstance().isModLoaded("fabric-lifecycle-events-v1")) {
            ServerLevelEvents.LOAD.register((server, world) -> ((ComponentProvider) world).getComponentContainer().onServerLoad());
            ServerLevelEvents.UNLOAD.register((server, world) -> ((ComponentProvider) world).getComponentContainer().onServerUnload());
        }
        StaticLevelComponentPlugin.INSTANCE.ensureInitialized();
    }

    public static ComponentContainer createComponents(Level world) {
        return levelContainerFactories.computeIfAbsent(
            world.dimension(),
            CardinalComponentsLevel::getLevelComponentFactory
        ).createContainer(world);
    }


    private static synchronized ComponentContainer.Factory<Level> getLevelComponentFactory(ResourceKey<Level> dimensionKey) {
        // need to check again despite synchronization, because the factory may have been generated while waiting from createComponents
        ComponentContainer.Factory<Level> existing = levelContainerFactories.get(dimensionKey);
        if (existing != null) return existing;

        if (StaticLevelComponentPlugin.INSTANCE.requiresStaticFactory(dimensionKey)) {
            return StaticLevelComponentPlugin.INSTANCE.buildDedicatedFactory(dimensionKey);
        } else {
            return levelContainerFactories.computeIfAbsent(null, n -> StaticLevelComponentPlugin.INSTANCE.buildDedicatedFactory(null));
        }
    }
}
