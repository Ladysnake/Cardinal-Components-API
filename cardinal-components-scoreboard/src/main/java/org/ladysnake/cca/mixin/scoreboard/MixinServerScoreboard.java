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
package org.ladysnake.cca.mixin.scoreboard;

import com.mojang.datafixers.util.Unit;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.NbtReadView;
import net.minecraft.util.ErrorReporter;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.TransientComponent;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.scoreboard.TeamAddCallback;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.base.ComponentsInternals;
import org.ladysnake.cca.internal.scoreboard.CardinalComponentsScoreboard;
import org.ladysnake.cca.internal.scoreboard.CcaPackedState;
import org.ladysnake.cca.internal.scoreboard.ScoreboardComponentContainerFactory;
import org.ladysnake.cca.internal.scoreboard.StaticScoreboardComponentPlugin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ServerScoreboard.class)
public abstract class MixinServerScoreboard extends MixinScoreboard {
    @Unique
    private static final Supplier<ScoreboardComponentContainerFactory> componentsContainerFactory = StaticScoreboardComponentPlugin.scoreboardComponentsContainerFactory;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    private boolean dirty;

    @Override
    protected Team unpackComponents(Team team, CcaPackedState packedTeam) {
        NbtCompound nbt = packedTeam.cca$getSerializedComponents();
        if (nbt != null) {
            try (var errorReporter = new ErrorReporter.Logging(ComponentsInternals.LOGGER)) {
                ((ComponentProvider) team).getComponentContainer().readOrphanData(NbtReadView.create(errorReporter, server.getRegistryManager(), nbt));
            }
        }
        return super.unpackComponents(team, packedTeam);
    }

    @Override
    public Iterable<ServerPlayerEntity> getRecipientsForComponentSync() {
        MinecraftServer server = this.server;

        if (server.getPlayerManager() != null) {
            return server.getPlayerManager().getPlayerList();
        }
        return List.of();
    }

    @Override
    public <C extends AutoSyncedComponent> ComponentUpdatePayload<?> toComponentPacket(ComponentKey<? super C> key, boolean required, RegistryByteBuf data) {
        return new ComponentUpdatePayload<>(
            CardinalComponentsScoreboard.SCOREBOARD_PACKET_ID,
            Unit.INSTANCE,
            required,
            key.getId(),
            data
        );
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initComponents(CallbackInfo ci) {
        this.components = componentsContainerFactory.get().create((Scoreboard) (Object) this, this.server);
    }

    @Inject(method = "updateScoreboardTeamAndPlayers", at = @At("RETURN"))
    private void syncTeamComponents(Team team, CallbackInfo ci) {
        TeamAddCallback.EVENT.invoker().onTeamAdded(team);
    }

    @Inject(method = "writeTo", at = @At("HEAD"))
    private void setDirty(ScoreboardState state, CallbackInfo ci) {
        if (components.hasComponents()) {
            for (var component : components.keys()) {
                if (!(component instanceof TransientComponent)) {
                    dirty = true;
                    return;
                }
            }
        }
    }

    @ModifyArg(method = "writeTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardState;set(Lnet/minecraft/scoreboard/ScoreboardState$Packed;)V"))
    private ScoreboardState.Packed writeComponents(ScoreboardState.Packed packed) {
        ((CcaPackedState) (Object) packed).cca$setSerializedComponents(
            components.toOrphanTag(server.getRegistryManager())
        );

        return packed;
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void readComponents(ScoreboardState.Packed packed, CallbackInfo ci) {
        NbtCompound nbt = ((CcaPackedState) (Object) packed).cca$getSerializedComponents();
        if (nbt != null) {
            try (var errorReporter = new ErrorReporter.Logging(ComponentsInternals.LOGGER)) {
                components.readOrphanData(NbtReadView.create(errorReporter, server.getRegistryManager(), nbt));
            }
        }
    }
}
