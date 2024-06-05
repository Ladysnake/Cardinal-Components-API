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
package org.ladysnake.cca.test.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.test.base.SyncedVita;
import org.ladysnake.cca.test.base.Vita;

import java.util.EnumMap;
import java.util.Map;

public class VitaCompound implements AutoSyncedComponent {
    public static final ComponentKey<VitaCompound> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of(CcaBlockTestMod.MOD_ID, "vita_compound"), VitaCompound.class);

    private final Map<Direction, SyncedVita> storage = new EnumMap<>(Direction.class);

    public VitaCompound(BlockEntity owner) {
        for (Direction side : Direction.values()) {
            storage.put(side, new SyncedVita(owner));
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        for (Map.Entry<Direction, SyncedVita> entry : this.storage.entrySet()) {
            if (tag.contains(entry.getKey().name(), NbtElement.COMPOUND_TYPE)) {
                entry.getValue().readFromNbt(tag.getCompound(entry.getKey().name()), registryLookup);
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        storage.forEach((side, vita) -> tag.put(side.name(), Util.make(new NbtCompound(), tag1 -> vita.writeToNbt(tag1, registryLookup))));
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        for (SyncedVita value : this.storage.values()) {
            if (value.shouldSyncWith(player)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        for (SyncedVita value : this.storage.values()) {
            if (value.shouldSyncWith(recipient)) {
                value.writeSyncPacket(buf, recipient);
            }
        }
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        for (SyncedVita value : this.storage.values()) {
            value.applySyncPacket(buf);
        }
    }

    public Vita get(Direction side) {
        return storage.get(side);
    }
}
