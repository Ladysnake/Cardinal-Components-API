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
package nerdhub.cardinal.componentstest.vita;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.sync.WorldSyncedComponent;
import nerdhub.cardinal.componentstest.CardinalComponentsTest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Objects;

public class WorldVita extends BaseVita implements WorldSyncedComponent {
    private World world;

    public WorldVita(World world) {
        this.world = world;
    }

    @Override
    public void setVitality(int value) {
        super.setVitality(value);
        if (world.isClient) {
            // never do this in your mods.
            String worldName = Objects.requireNonNull(DimensionType.getId(world.getDimension().getType())).getPath().replace('_', ' ');
            String worldVita = I18n.translate("componenttest:title.world_vitality", this.getVitality());
            MinecraftClient.getInstance().inGameHud.setTitles(null, worldVita, -1, -1, -1);
            MinecraftClient.getInstance().inGameHud.setTitles(worldName, null, -1, -1, -1);
        } else {
            markDirty();
        }
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public ComponentType<?> getComponentType() {
        return CardinalComponentsTest.VITA;
    }

    @Override
    public void writeToPacket(PacketByteBuf buf) {
        buf.writeInt(this.getVitality());
    }

    @Override
    public void readFromPacket(PacketByteBuf buf) {
        this.setVitality(buf.readInt());
    }
}
