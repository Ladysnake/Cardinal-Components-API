/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.cca.api.v3.component.sync;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;

/**
 * A {@link Component} implementing this interface will have its data automatically
 * synchronized with players watching its provider.
 *
 * @see ComponentKey#sync(Object)
 */
public interface AutoSyncedComponent extends Component, ComponentPacketWriter, PlayerSyncPredicate {

    /**
     * Returns {@code true} if a synchronization packet for this component
     * should be immediately sent to {@code player}.
     *
     * @param player potential recipient of a synchronization packet
     * @return {@code true} if synchronization with the {@code player} should occur,
     * {@code false} otherwise
     */
    @Override
    default boolean shouldSyncWith(ServerPlayerEntity player) {
        return true;
    }

    /**
     * Writes this component's data to {@code buf}.
     *
     * <p>Implementations may choose to change the data written to the packet based on the {@code recipient}
     * or on the {@code syncOp}, or they may safely ignore both parameters.
     * A {@code syncOp} value of {@code 0} triggers the base full synchronization behaviour.
     * Other values have a meaning specific to the component implementation.
     * Typical uses of this parameter include limiting the amount of data being synced (eg. by identifying a
     * specific field to write), or triggering different events on the client (similar to {@link World#sendEntityStatus(Entity, byte)})
     *
     * @param buf       the buffer to write the data to
     * @param recipient the player to which the packet will be sent
     * @implSpec The default implementation writes the whole NBT representation
     * of this component to the buffer using {@link #writeToNbt(NbtCompound)}.
     * @implNote The default implementation should generally be overridden.
     * The serialization done by the default implementation sends possibly hidden
     * information to clients, uses a wasteful data format, and does not support
     * any optimization such as incremental updates. Implementing classes can
     * nearly always provide a better implementation.
     * @see ComponentKey#sync(Object)
     * @see ComponentKey#sync(Object, ComponentPacketWriter)
     * @see #applySyncPacket(PacketByteBuf)
     */
    @Contract(mutates = "param1")
    @Override
    default void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        NbtCompound tag = new NbtCompound();
        this.writeToNbt(tag);
        buf.writeNbt(tag);
    }

    /**
     * Reads this component's data from {@code buf}.
     *
     * @implSpec The default implementation converts the buffer's content
     * to a {@link NbtCompound} and calls {@link #readFromNbt(NbtCompound)}.
     * @implNote any implementing class overriding {@link #writeSyncPacket(PacketByteBuf, ServerPlayerEntity)}
     * such that it uses a different data format must override this method.
     * @see #writeSyncPacket(PacketByteBuf, ServerPlayerEntity)
     */
    @CheckEnv(Env.CLIENT)
    default void applySyncPacket(PacketByteBuf buf) {
        NbtCompound tag = buf.readNbt();
        if (tag != null) {
            this.readFromNbt(tag);
        }
    }
}
