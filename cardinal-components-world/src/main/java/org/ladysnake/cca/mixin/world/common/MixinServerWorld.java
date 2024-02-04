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
package org.ladysnake.cca.mixin.world.common;

import com.mojang.datafixers.util.Unit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentStateManager;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.world.CardinalComponentsWorld;
import org.ladysnake.cca.internal.world.ComponentPersistentState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends MixinWorld {
    @Shadow public abstract PersistentStateManager getPersistentStateManager();

    @Shadow
    public abstract List<ServerPlayerEntity> getPlayers();

    @Unique
    private static final String PERSISTENT_STATE_KEY = "cardinal_world_components";

    @Inject(at = @At("RETURN"), method = "<init>*")
    private void constructor(CallbackInfo ci) {
        try {
            ComponentPersistentState.LOADING.set(true);
            this.getPersistentStateManager().getOrCreate(
                ComponentPersistentState.getType(this.components),
                PERSISTENT_STATE_KEY
            );
        } finally {
            ComponentPersistentState.LOADING.set(false);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.getComponentContainer().tickServerComponents();
    }

    @Override
    public Iterable<ServerPlayerEntity> getRecipientsForComponentSync() {
        return this.getPlayers();
    }

    @Override
    public <C extends AutoSyncedComponent> ComponentUpdatePayload<?> toComponentPacket(ComponentKey<? super C> key, RegistryByteBuf data) {
        return new ComponentUpdatePayload<>(
            CardinalComponentsWorld.PACKET_ID,
            Unit.INSTANCE,
            key,
            data
        );
    }
}
