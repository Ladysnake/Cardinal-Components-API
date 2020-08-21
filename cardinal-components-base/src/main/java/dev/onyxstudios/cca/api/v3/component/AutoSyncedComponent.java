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

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;

/**
 * A {@link Component} that can be written to and read from a {@link PacketByteBuf}.
 */
public interface AutoSyncedComponent extends ComponentV3 {
    default boolean shouldSyncWith(ServerPlayerEntity player) {
        return true;
    }

    /**
     * Writes this component's data to {@code buf}.
     *
     * <p>The data written to the packet may differ based on the {@code recipient}.
     *
     * @param buf       the buffer to write the data to
     * @param recipient the player to which the packet will be sent
     * @implSpec The default implementation writes the whole NBT representation
     * of this component to the buffer using {@link #writeToNbt(CompoundTag)}.
     * @implNote The default implementation should generally be overridden.
     * The serialization done by the default implementation sends possibly hidden
     * information to clients, uses a wasteful data format, and does not support
     * any optimization such as incremental updates. Implementing classes can
     * nearly always provide a better implementation.
     * @see #readFromPacket(PacketByteBuf)
     */
    default void writeToPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeCompoundTag(Util.make(new CompoundTag(), this::writeToNbt));
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
    default void readFromPacket(PacketByteBuf buf) {
        CompoundTag tag = buf.readCompoundTag();
        if (tag != null) {
            this.readFromNbt(tag);
        }
    }
}
