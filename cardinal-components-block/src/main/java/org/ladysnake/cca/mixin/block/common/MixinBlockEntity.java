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
package org.ladysnake.cca.mixin.block.common;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.internal.BlockEntityAddress;
import org.ladysnake.cca.internal.CardinalComponentsBlock;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.block.CardinalBlockInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements ComponentProvider {
    @Shadow
    @Nullable
    public abstract Level getLevel();

    @Shadow
    public abstract BlockPos getBlockPos();

    @Shadow
    public abstract BlockEntityType<?> getType();

    @Unique
    private ComponentContainer components;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        // Promise, this is a BlockEntity
        //noinspection ConstantConditions
        this.components = CardinalBlockInternals.createComponents((BlockEntity) (Object) this);
    }

    @Inject(method = {"saveWithoutMetadata(Lnet/minecraft/world/level/storage/ValueOutput;)V", "saveCustomOnly(Lnet/minecraft/world/level/storage/ValueOutput;)V"}, at = @At("RETURN"))
    private void writeNbt(ValueOutput data, CallbackInfo ci) {
        this.components.writeData(data);
    }

    @Inject(method = "loadWithComponents", at = @At(value = "RETURN"))
    private void readNbt(ValueInput view, CallbackInfo ci) {
        this.components.readData(view);
    }

    @Override
    public ComponentContainer getComponentContainer() {
        return this.components;
    }

    @Override
    public Iterable<ServerPlayer> getRecipientsForComponentSync() {
        Level world = this.getLevel();

        if (world != null && !world.isClientSide()) {
            return PlayerLookup.tracking((BlockEntity) (Object) this);
        }
        return List.of();
    }

    @Override
    public <C extends AutoSyncedComponent> @Nullable ComponentUpdatePayload<?> toComponentPacket(ComponentKey<? super C> key, boolean required, RegistryFriendlyByteBuf data) {
        Level world = this.getLevel();
        if (world == null) {
            return null;
        }

        return new ComponentUpdatePayload<>(
            CardinalComponentsBlock.PACKET_ID,
            new BlockEntityAddress(this.getType(), this.getBlockPos(), world.dimension()),
            required,
            key.getId(),
            data.array()
        );
    }
}
