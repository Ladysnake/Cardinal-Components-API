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
package org.ladysnake.cca.mixin.entity.common;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
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

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Mixin(Entity.class)
public abstract class MixinEntity implements ComponentProvider {
    @Unique
    private ComponentContainer components;

    @Shadow
    private World world;

    @Shadow public abstract int getId();

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void initDataTracker(CallbackInfo ci) {
        this.components = CardinalEntityInternals.createEntityComponentContainer((Entity) (Object) this);
    }

    @Inject(method = "writeData", at = @At("RETURN"))
    private void toTag(WriteView view, CallbackInfo ci) {
        this.components.writeData(view);
    }

    @Inject(method = "readData", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomData(Lnet/minecraft/storage/ReadView;)V", shift = At.Shift.AFTER))
    private void fromTag(ReadView view, CallbackInfo ci) {
        this.components.readData(view);
    }

    @Nonnull
    @Override
    public ComponentContainer getComponentContainer() {
        return this.components;
    }

    @Override
    public Iterable<ServerPlayerEntity> getRecipientsForComponentSync() {
        Entity holder = (Entity) (Object) this;
        if (!this.world.isClient()) {
            Deque<ServerPlayerEntity> watchers = new ArrayDeque<>(PlayerLookup.tracking(holder));
            if (holder instanceof ServerPlayerEntity player && player.networkHandler != null) {
                watchers.addFirst(player);
            }
            return watchers;
        }
        return List.of();
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")   // it's okay, we have custom types in the descriptor
    @Override
    public <C extends AutoSyncedComponent> ComponentUpdatePayload<?> toComponentPacket(ComponentKey<? super C> key, boolean required, RegistryByteBuf data) {
        return new ComponentUpdatePayload<>(
            CardinalComponentsEntity.PACKET_ID,
            this.getId(),
            required,
            key.getId(),
            data
        );
    }
}
