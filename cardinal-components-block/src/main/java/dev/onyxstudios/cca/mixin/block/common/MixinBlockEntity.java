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
package dev.onyxstudios.cca.mixin.block.common;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.sync.ComponentPacketWriter;
import dev.onyxstudios.cca.internal.CardinalComponentsBlock;
import dev.onyxstudios.cca.internal.block.CardinalBlockInternals;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements ComponentProvider {
    @Shadow
    @Nullable
    public abstract World getWorld();

    @Shadow
    public abstract BlockPos getPos();

    @Shadow
    public abstract BlockEntityType<?> getType();

    @Unique
    private ComponentContainer components;

    @Inject(method = "createFromNbt", at = @At("RETURN"))
    private static void readComponentData(BlockPos pos, BlockState state, NbtCompound nbt, CallbackInfoReturnable<BlockEntity> cir) {
        if (cir.getReturnValue() != null) {
            cir.getReturnValue().asComponentProvider().getComponentContainer().fromTag(nbt);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        // Promise, this is a BlockEntity
        //noinspection ConstantConditions
        this.components = CardinalBlockInternals.createComponents((BlockEntity) (Object) this);
    }

    @Inject(method = "createNbt", at = @At("RETURN"))
    private void writeNbt(CallbackInfoReturnable<NbtCompound> cir) {
        this.components.toTag(cir.getReturnValue());
    }

    /**
     * Yay redundancy!
     *
     * <p>This method may be overridden without calling super(), so we need safety nets.
     * On the other hand, we still need this inject because mods can also call {@link BlockEntity#readNbt(NbtCompound)} directly.
     * We mostly do not care about what happens on the client though, since we have our own packets.
     *
     * @see #readComponentData(BlockPos, BlockState, NbtCompound, CallbackInfoReturnable)
     * @see MixinBlockDataObject#readComponentData(NbtCompound, CallbackInfo)
     */
    @Inject(method = "readNbt", at = @At(value = "RETURN"))
    private void readNbt(NbtCompound tag, CallbackInfo ci) {
        this.components.fromTag(tag);
    }

    @Nonnull
    @Override
    public ComponentContainer getComponentContainer() {
        return this.components;
    }

    @Override
    public Iterable<ServerPlayerEntity> getRecipientsForComponentSync() {
        World world = this.getWorld();

        if (world != null && !world.isClient) {
            return PlayerLookup.tracking((BlockEntity) (Object) this);
        }
        return List.of();
    }

    @Nullable
    @Override
    public <C extends AutoSyncedComponent> CustomPayloadS2CPacket toComponentPacket(ComponentKey<? super C> key, ComponentPacketWriter writer, ServerPlayerEntity recipient) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(BlockEntityType.getId(this.getType()));
        buf.writeBlockPos(this.getPos());
        buf.writeIdentifier(key.getId());
        writer.writeSyncPacket(buf, recipient);
        return (CustomPayloadS2CPacket) ServerPlayNetworking.createS2CPacket(CardinalComponentsBlock.PACKET_ID, buf);
    }

}
