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
package nerdhub.cardinal.componentstest.vita;

import nerdhub.cardinal.components.api.util.sync.BaseSyncedComponent;
import nerdhub.cardinal.components.api.util.sync.LevelSyncedComponent;
import nerdhub.cardinal.components.api.util.sync.WorldSyncedComponent;
import nerdhub.cardinal.componentstest.CardinalTestComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Objects;

public abstract class AmbientVita extends BaseVita implements BaseSyncedComponent {

    public abstract void syncWithAll(MinecraftServer server);

    @Override
    public void readFromPacket(PacketByteBuf buf) {
        int vita = buf.readInt();
        this.setVitality(vita);
        World world = Objects.requireNonNull(MinecraftClient.getInstance().player).world;
        // Very bad shortcut to get a dimension's name
        Text worldName = new LiteralText(
            Objects.requireNonNull(DimensionType.getId(world.getDimension().getType())).getPath().replace('_', ' ')
        );
        Text worldVita = new TranslatableText(
                "componenttest:title.world_vitality",
                CardinalTestComponents.VITA.get(world).getVitality(),
                CardinalTestComponents.VITA.get(world.getLevelProperties()).getVitality()
        );
        InGameHud inGameHud = MinecraftClient.getInstance().inGameHud;
        inGameHud.setTitles(null, worldVita, -1, -1, -1);
        inGameHud.setTitles(worldName, null, -1, -1, -1);
    }

    /**
     * proper implementation of {@code writeToPacket}, writes a single int instead of a whole tag
     */
    @Override
    public void writeToPacket(PacketByteBuf buf) {
        buf.writeInt(this.getVitality());
    }

    /**
     * Implements markDirty and syncWith through {@code WorldSyncedComponent}
     */
    public static class WorldVita extends AmbientVita implements WorldSyncedComponent {
        private final World world;

        public WorldVita(World world) {
            this.world = world;
        }

        @Override
        public World getWorld() {
            return this.world;
        }

        @Override
        public void syncWithAll(MinecraftServer server) {
            WorldSyncedComponent.super.sync();
        }
    }

    /**
     * Implements markDirty and syncWith through {@code LevelSyncedComponent}
     */
    public static class LevelVita extends AmbientVita implements LevelSyncedComponent {
        @Override
        public void syncWithAll(MinecraftServer server) {
            LevelSyncedComponent.super.syncWithAll(server);
        }
    }
}
