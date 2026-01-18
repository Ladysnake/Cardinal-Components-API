/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2025 Ladysnake
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
package org.ladysnake.cca.test.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.test.base.BaseVita;
import org.ladysnake.cca.test.base.CardinalGameTest;
import org.ladysnake.cca.test.base.Vita;

import java.util.Objects;

public abstract class AmbientVita extends BaseVita implements AutoSyncedComponent {

    public abstract void syncWithAll(MinecraftServer server);

    @Override
    public void applySyncPacket(RegistryFriendlyByteBuf buf) {
        int vita = buf.readInt();
        this.setVitality(vita);
        Level world = Objects.requireNonNull(Minecraft.getInstance().player).level();
        // Very bad shortcut to get a dimension's name
        Component worldName = Component.literal(
            Objects.requireNonNull(world.dimension() == Level.OVERWORLD ? "Overworld" : "Alien World")
        );
        Component worldVita = Component.translatable(
                "componenttest:title.world_vitality",
                Vita.get(world).getVitality(),
                Vita.get(world.getLevelData()).getVitality()
        );
        Gui inGameHud = Minecraft.getInstance().gui;
        inGameHud.setTimes(-1, -1, -1);
        inGameHud.setTitle(worldName);
        inGameHud.setSubtitle(worldVita);
    }

    /**
     * proper implementation of {@code writeToPacket}, writes a single int instead of a whole tag
     */
    @Override
    public void writeSyncPacket(RegistryFriendlyByteBuf buf, ServerPlayer player) {
        buf.writeInt(this.getVitality());
    }

    public static class WorldVita extends AmbientVita implements ClientTickingComponent {
        private final Level world;

        public WorldVita(Level world) {
            this.world = world;
        }

        @Override
        public boolean isRequiredOnClient() {
            return false;
        }

        @Override
        public void syncWithAll(MinecraftServer server) {
            this.world.syncComponent(KEY);
        }

        @Override
        public void clientTick() {
            if (this.world.getGameTime() % 2400 == 0) {
                CardinalGameTest.LOGGER.info("The world still runs, and is now worth {}", this.vitality);
            }
        }
    }

}
