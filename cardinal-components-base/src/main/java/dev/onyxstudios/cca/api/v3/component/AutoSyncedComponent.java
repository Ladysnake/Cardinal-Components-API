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
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

/**
 * A {@link Component} implementing this interface will have its data automatically
 * synchronized with players watching its provider.
 *
 * @see ComponentKey#sync(Object)
 */
// TODO consider renaming packet methods to writeSyncPacket and applySyncPacket
public interface AutoSyncedComponent extends Component {
    /**
     * The default sync operation.
     *
     * <p>When {@link #writeToPacket(PacketByteBuf, ServerPlayerEntity, int)} is called with this value,
     * all synchronized data should be written to the packet as if the recipient got it for the first time.
     */
    int FULL_SYNC = 0;

    /**
     * @deprecated use/override {@link #shouldSyncWith(ServerPlayerEntity, int)} instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default boolean shouldSyncWith(ServerPlayerEntity player) {
        return true;
    }

    /**
     * Returns {@code true} if a synchronization packet for this component
     * should be immediately sent to {@code player}.
     *
     * @param player potential recipient of a synchronization packet
     * @param syncOp the specific sync operation to be performed, as passed in {@link ComponentKey#sync(Object, int)}
     * @return {@code true} if synchronization with the {@code player} should occur,
     * {@code false} otherwise
     */
    @Contract(pure = true)
    default boolean shouldSyncWith(ServerPlayerEntity player, int syncOp) {
        // calling the deprecated overload for backward compatibility
        return this.shouldSyncWith(player);
    }

    /**
     * @deprecated use/override {@link #writeToPacket(PacketByteBuf, ServerPlayerEntity, int)}
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    default void writeToPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeCompoundTag(Util.make(new CompoundTag(), this::writeToNbt));
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
     * @param syncOp    the specific sync operation to be performed, as passed in {@link ComponentKey#sync(Object, int)}
     * @implSpec The default implementation writes the whole NBT representation
     * of this component to the buffer using {@link #writeToNbt(CompoundTag)}.
     * @implNote The default implementation should generally be overridden.
     * The serialization done by the default implementation sends possibly hidden
     * information to clients, uses a wasteful data format, and does not support
     * any optimization such as incremental updates. Implementing classes can
     * nearly always provide a better implementation.
     * @see ComponentKey#sync(Object)
     * @see ComponentKey#sync(Object, int)
     * @see #readFromPacket(PacketByteBuf)
     */
    @Contract(mutates = "param1")
    default void writeToPacket(PacketByteBuf buf, ServerPlayerEntity recipient, int syncOp) {
        // calling the deprecated overload for backward compatibility
        this.writeToPacket(buf, recipient);
    }

    /**
     * Reads this component's data from {@code buf}.
     *
     * @implSpec The default implementation converts the buffer's content
     * to a {@link CompoundTag} and calls {@link #fromTag(CompoundTag)}.
     * @implNote any implementing class overriding {@link #writeToPacket(PacketByteBuf, ServerPlayerEntity)}
     * such that it uses a different data format must override this method.
     * @see #writeToPacket(PacketByteBuf, ServerPlayerEntity)
     */
    @CheckEnv(Env.CLIENT)
    default void readFromPacket(PacketByteBuf buf) {
        CompoundTag tag = buf.readCompoundTag();
        if (tag != null) {
            this.readFromNbt(tag);
        }
    }
}
