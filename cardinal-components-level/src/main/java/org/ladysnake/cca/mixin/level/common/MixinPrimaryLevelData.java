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
package org.ladysnake.cca.mixin.level.common;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.TagValueOutput;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.internal.base.AbstractComponentContainer;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.base.ComponentsInternals;
import org.ladysnake.cca.internal.level.CardinalComponentsLevel;
import org.ladysnake.cca.internal.level.StaticLevelComponentPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;

@Mixin(PrimaryLevelData.class)
public abstract class MixinPrimaryLevelData implements ServerLevelData, ComponentProvider {
    @Unique
    private ComponentContainer components;

    @Inject(method = "<init>(Ljava/util/UUID;ZLnet/minecraft/world/level/storage/LevelData$RespawnData;JIZLjava/util/Set;Ljava/util/Set;Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/storage/PrimaryLevelData$SpecialWorldProperty;Lcom/mojang/serialization/Lifecycle;)V", at = @At("RETURN"))
    private void initComponents(UUID singlePlayerUUID, boolean wasModded, RespawnData respawnData, long gameTime, int version, boolean initialized, Set<?> knownServerBrands, Set<?> removedFeatureFlags, LevelSettings settings, PrimaryLevelData.SpecialWorldProperty specialWorldProperty, Lifecycle worldGenSettingsLifecycle, CallbackInfo ci)  {
        this.components = StaticLevelComponentPlugin.createContainer(this);
    }

    @Inject(method = "setTagData", at = @At("RETURN"))
    private void writeComponents(CompoundTag data, UUID singlePlayerUUID, CallbackInfo ci) {
        try (var errorReporter = new ProblemReporter.ScopedCollector(ComponentsInternals.LOGGER)) {
            TagValueOutput writeView = TagValueOutput.createWithoutContext(errorReporter);
            this.components.writeOrphanData(writeView);
            if (!writeView.isEmpty()) {
                data.put(AbstractComponentContainer.NBT_KEY, writeView.buildResult());
            }
        }
    }

    @Nonnull
    @Override
    public ComponentContainer getComponentContainer() {
        return this.components;
    }

    @Override
    public Iterable<ServerPlayer> getRecipientsForComponentSync() {
        throw new UnsupportedOperationException("Please call LevelComponents#sync(MinecraftServer) instead of ComponentKey#sync");
    }

    @Override
    public <C extends AutoSyncedComponent> ComponentUpdatePayload<?> toComponentPacket(ComponentKey<? super C> key, boolean required, RegistryFriendlyByteBuf data) {
        return new ComponentUpdatePayload<>(
            CardinalComponentsLevel.PACKET_ID,
            Unit.INSTANCE,
            required,
            key.getId(),
            data
        );
    }

}
