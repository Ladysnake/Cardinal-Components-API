/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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
package dev.onyxstudios.cca.mixin.level.common;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.sync.ComponentPacketWriter;
import dev.onyxstudios.cca.internal.level.CardinalComponentsLevel;
import dev.onyxstudios.cca.internal.level.StaticLevelComponentPlugin;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.timer.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

@Mixin(LevelProperties.class)
public abstract class MixinLevelProperties implements ServerWorldProperties, ComponentProvider {
    @Unique
    private ComponentContainer components;

    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;ZIIIFJJIIIZIZZZLnet/minecraft/world/border/WorldBorder$Properties;IILjava/util/UUID;Ljava/util/Set;Ljava/util/Set;Lnet/minecraft/world/timer/Timer;Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/entity/boss/dragon/EnderDragonFight$Data;Lnet/minecraft/world/level/LevelInfo;Lnet/minecraft/world/gen/GeneratorOptions;Lnet/minecraft/world/level/LevelProperties$SpecialProperty;Lcom/mojang/serialization/Lifecycle;)V", at = @At("RETURN"))
    private void initComponents(NbtCompound playerData, boolean modded, int spawnX, int spawnY, int spawnZ, float spawnAngle, long time, long timeOfDay, int version, int clearWeatherTime, int rainTime, boolean raining, int thunderTime, boolean thundering, boolean initialized, boolean difficultyLocked, WorldBorder.Properties worldBorder, int wanderingTraderSpawnDelay, int wanderingTraderSpawnChance, UUID wanderingTraderId, Set<?> serverBrands, Set<?> removedFeatures, Timer<?> scheduledEvents, NbtCompound customBossEvents, EnderDragonFight.Data dragonFight, LevelInfo levelInfo, GeneratorOptions generatorOptions, LevelProperties.SpecialProperty specialProperty, Lifecycle lifecycle, CallbackInfo ci)  {
        this.components = StaticLevelComponentPlugin.createContainer(this);
    }

    @Inject(method = "readProperties", at = @At("RETURN"))
    private static <T> void readComponents(Dynamic<T> dynamic, LevelInfo info, LevelProperties.SpecialProperty specialProperty, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> cir) {
        ((ComponentProvider) cir.getReturnValue()).getComponentContainer().fromDynamic(dynamic);
    }

    @Inject(method = "updateProperties", at = @At("RETURN"))
    private void writeComponents(DynamicRegistryManager tracker, NbtCompound data, NbtCompound player, CallbackInfo ci) {
        this.components.toTag(data);
    }

    @Nonnull
    @Override
    public ComponentContainer getComponentContainer() {
        return this.components;
    }

    @Override
    public Iterable<ServerPlayerEntity> getRecipientsForComponentSync() {
        throw new UnsupportedOperationException("Please call LevelComponents#sync(MinecraftServer) instead of ComponentKey#sync");
    }

    @Nullable
    @Override
    public <C extends AutoSyncedComponent> CustomPayloadS2CPacket toComponentPacket(ComponentKey<? super C> key, ComponentPacketWriter writer, ServerPlayerEntity recipient) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(key.getId());
        writer.writeSyncPacket(buf, recipient);
        return (CustomPayloadS2CPacket) ServerPlayNetworking.createS2CPacket(CardinalComponentsLevel.PACKET_ID, buf);
    }

}
