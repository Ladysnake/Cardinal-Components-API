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
package org.ladysnake.cca.mixin.entity.common;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void toTag(NbtCompound inputTag, CallbackInfoReturnable<NbtCompound> cir) {
        this.components.toTag(cir.getReturnValue());
    }

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
    private void fromTag(NbtCompound tag, CallbackInfo ci) {
        this.components.fromTag(tag);
    }

    @Nonnull
    @Override
    public ComponentContainer getComponentContainer() {
        return this.components;
    }

    @Override
    public Iterable<ServerPlayerEntity> getRecipientsForComponentSync() {
        Entity holder = (Entity) (Object) this;
        if (!this.world.isClient) {
            Deque<ServerPlayerEntity> watchers = new ArrayDeque<>(PlayerLookup.tracking(holder));
            if (holder instanceof ServerPlayerEntity player && player.networkHandler != null) {
                watchers.addFirst(player);
            }
            return watchers;
        }
        return List.of();
    }

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
