/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
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
package nerdhub.cardinal.components.api.util.sync;

import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.component.extension.TypeAwareComponent;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

/**
 * {@code SyncedComponent} extension with read and write methods that can respectively be used
 * as defaults in {@link #syncWith(ServerPlayerEntity)} and {@link #processPacket(PacketContext, PacketByteBuf)}
 * implementations.
 */
public interface BaseSyncedComponent extends SyncedComponent, TypeAwareComponent {

    /**
     * Write this component's data to {@code buf}.
     *
     * @implSpec The default implementation writes the whole NBT representation
     * of this component to the buffer using {@link #toTag(CompoundTag)}.
     * @implNote The default implementation should generally be overridden.
     * The serialization done by the default implementation sends possibly hidden
     * information to clients, uses a wasteful data format, and does not support
     * any optimization such as incremental updates. Implementing classes can 
     * nearly always provide a better implementation.
     * 
     * @see #readFromPacket(PacketByteBuf)
     */
    default void writeToPacket(PacketByteBuf buf) {
        buf.writeCompoundTag(this.toTag(new CompoundTag()));
    }

    /**
     * Reads this component's data from {@code buf}.
     *
     * @implSpec The default implementation converts the buffer's content
     * to a {@code TagCompound} and calls {@link #fromTag(CompoundTag)}.
     * @implNote any implementing class overriding {@link #writeToPacket(PacketByteBuf)}
     * such that it uses a different data format must override this method.
     *
     * @see #writeToPacket(PacketByteBuf)
     */
    default void readFromPacket(PacketByteBuf buf) {
        CompoundTag tag = buf.readCompoundTag();
        if (tag != null) {
            this.fromTag(tag);
        }
    }
}
