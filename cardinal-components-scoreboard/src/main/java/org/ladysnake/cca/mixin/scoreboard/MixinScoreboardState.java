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
package org.ladysnake.cca.mixin.scoreboard;

import org.ladysnake.cca.api.v3.component.ComponentProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.Iterator;

@Mixin(ScoreboardState.class)
public abstract class MixinScoreboardState {
    @Final
    @Shadow
    private Scoreboard scoreboard;

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void saveComponents(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
        ((ComponentProvider) this.scoreboard).getComponentContainer().toTag(tag);
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void loadComponents(NbtCompound tag, CallbackInfoReturnable<ScoreboardState> ci) {
        ((ComponentProvider) this.scoreboard).getComponentContainer().fromTag(tag);
    }

    @Inject(
        method = "readTeamsNbt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/scoreboard/ScoreboardState;readTeamPlayersNbt(Lnet/minecraft/scoreboard/Team;Lnet/minecraft/nbt/NbtList;)V",
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void loadTeamComponents(NbtList teamsData, CallbackInfo ci, int i, NbtCompound teamData, String name, Team team) {
        ((ComponentProvider) team).getComponentContainer().fromTag(teamData);
    }

    @Inject(
        method = "teamsToNbt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/scoreboard/Team;getPlayerList()Ljava/util/Collection;"
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void saveTeamComponents(
        CallbackInfoReturnable<NbtList> cir,
        NbtList teamsData,
        Collection<Team> teams,
        Iterator<Team> it,
        Team team,
        NbtCompound teamData
    ) {
        ((ComponentProvider) team).getComponentContainer().toTag(teamData);
    }
}
