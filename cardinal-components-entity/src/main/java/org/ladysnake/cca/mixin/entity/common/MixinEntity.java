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
package org.ladysnake.cca.mixin.entity.common;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.entity.CardinalComponentsEntity;
import org.ladysnake.cca.internal.entity.CardinalEntityInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Mixin(Entity.class)
public abstract class MixinEntity implements ComponentProvider {
    @Unique
    private ComponentContainer components;

    @Shadow
    private Level level;

    @Shadow public abstract int getId();

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void initDataTracker(CallbackInfo ci) {
        this.components = CardinalEntityInternals.createEntityComponentContainer((Entity) (Object) this);
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void toTag(ValueOutput view, CallbackInfo ci) {
        this.components.writeData(view);
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", shift = At.Shift.AFTER))
    private void fromTag(ValueInput view, CallbackInfo ci) {
        this.components.readData(view);
    }

    @Override
    public ComponentContainer getComponentContainer() {
        return this.components;
    }

    @Override
    public Iterable<ServerPlayer> getRecipientsForComponentSync() {
        Entity holder = (Entity) (Object) this;
        if (!this.level.isClientSide()) {
            Deque<ServerPlayer> watchers = new ArrayDeque<>(PlayerLookup.tracking(holder));
            if (holder instanceof ServerPlayer player && player.connection != null) {
                watchers.addFirst(player);
            }
            return watchers;
        }
        return List.of();
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")   // it's okay, we have custom types in the descriptor
    @Override
    public <C extends AutoSyncedComponent> ComponentUpdatePayload<?> toComponentPacket(ComponentKey<? super C> key, boolean required, RegistryFriendlyByteBuf data) {
        return new ComponentUpdatePayload<>(
            CardinalComponentsEntity.PACKET_ID,
            this.getId(),
            required,
            key.getId(),
            data
        );
    }
}
