/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 OnyxStudios
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
package dev.onyxstudios.cca.api.v3.component;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import dev.onyxstudios.cca.api.v3.component.sync.ComponentPacketWriter;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

/**
 * A {@link Component} implementing this interface will have its data automatically
 * synchronized with players watching its provider.
 *
 * @deprecated implement {@link dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent} instead
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public interface AutoSyncedComponent extends dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent {
    /**
     * The default sync operation.
     *
     * <p>When {@link #writeToPacket(PacketByteBuf, ServerPlayerEntity, int)} is called with this value,
     * all synchronized data should be written to the packet as if the recipient got it for the first time.
     */
    int FULL_SYNC = 0;

    @Override
    default boolean shouldSyncWith(ServerPlayerEntity player) {
        return this.shouldSyncWith(player, FULL_SYNC);
    }

    /**
     * Returns {@code true} if a synchronization packet for this component
     * should be immediately sent to {@code player}.
     *
     * @param player potential recipient of a synchronization packet
     * @return {@code true} if synchronization with the {@code player} should occur,
     * {@code false} otherwise
     */
    @Contract(pure = true)
    default boolean shouldSyncWith(ServerPlayerEntity player, int syncOp) {
        return dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent.super.shouldSyncWith(player);
    }

    @Override
    default void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        this.writeToPacket(buf, recipient, FULL_SYNC);
    }

    /**
     * @deprecated override {@link dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent#writeSyncPacket(PacketByteBuf, ServerPlayerEntity)} instead.
     * <p>If you use {@code syncOp}, pass the extra information through a custom {@link ComponentPacketWriter} in {@link ComponentKey#sync(Object, ComponentPacketWriter)}
     */
    @Contract(mutates = "param1")
    default void writeToPacket(PacketByteBuf buf, ServerPlayerEntity recipient, int syncOp) {
        dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent.super.writeSyncPacket(buf, recipient);
    }

    @Override
    default void applySyncPacket(PacketByteBuf buf) {
        this.readFromPacket(buf);
    }

    /**
     * @deprecated override {@link dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent#applySyncPacket(PacketByteBuf)} instead
     */
    @CheckEnv(Env.CLIENT)
    default void readFromPacket(PacketByteBuf buf) {
        dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent.super.applySyncPacket(buf);
    }
}
