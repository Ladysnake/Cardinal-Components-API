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
package org.ladysnake.cca.test.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.sync.PlayerSyncPredicate;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import org.ladysnake.cca.api.v3.entity.C2SSelfMessagingComponent;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;
import org.ladysnake.cca.test.base.BaseVita;
import org.ladysnake.cca.test.base.CardinalGameTest;
import org.ladysnake.cca.test.base.Vita;

/**
 * A Vita component attached to players, and automatically synchronized with their owner
 */
public class PlayerVita extends EntityVita implements AutoSyncedComponent, ServerTickingComponent, RespawnableComponent<BaseVita>, C2SSelfMessagingComponent {
    public static final int INCREASE_VITA = 0b10;
    public static final int DECREASE_VITA = 0b100;

    public PlayerVita(PlayerEntity owner) {
        super(owner, 0);
    }

    @Override
    public void setVitality(int value) {
        if (value != this.vitality) {
            int increase = value - this.vitality;
            super.setVitality(value);
            this.owner.syncComponent(Vita.KEY, (buf, recipient) -> this.writeSyncPacket(buf, recipient, increase), PlayerSyncPredicate.all());
        }
    }

    @Override
    public void serverTick() {
        if (this.owner.age % 1200 == 0) {
            CardinalGameTest.LOGGER.info("{} is still alive", this.owner);
        }
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.owner;
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        this.writeSyncPacket(buf, recipient, 0);
    }

    private void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient, int increase) {
        boolean fullSync = recipient == this.owner;
        int flags = (fullSync ? 1 : 0) | (increase > 0 ? INCREASE_VITA : increase < 0 ? DECREASE_VITA : 0);
        buf.writeByte(flags);
        if (fullSync) {
            buf.writeVarInt(this.vitality);
        }
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        int flags = buf.readByte();
        if ((flags & 1) != 0) {
            this.vitality = buf.readVarInt();
        }
        if ((flags & INCREASE_VITA) != 0) {
            MinecraftClient.getInstance().particleManager.addEmitter(this.owner, ParticleTypes.TOTEM_OF_UNDYING, 30);
        } else if ((flags & DECREASE_VITA) != 0) {
            MinecraftClient.getInstance().particleManager.addEmitter(this.owner, ParticleTypes.ASH, 30);
        }
    }

    @Override
    public boolean shouldCopyForRespawn(boolean lossless, boolean keepInventory, boolean switchingCharacter) {
        return lossless || keepInventory;
    }

    @Override
    public void copyForRespawn(@NotNull BaseVita original, RegistryWrapper.WrapperLookup registryLookup, boolean lossless, boolean keepInventory, boolean switchingCharacter) {
        RespawnableComponent.super.copyForRespawn(original, registryLookup, lossless, keepInventory, switchingCharacter);
        if (!lossless && !keepInventory) {
            this.vitality -= 5;
        }
    }

    @Override
    public void handleC2SMessage(RegistryByteBuf buf) {
        ((PlayerEntity) this.owner).sendMessage(Text.of("Sync!"), true);
    }
}
