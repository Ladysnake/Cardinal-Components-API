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
package org.ladysnake.cca.api.v3.component.sync;

import net.fabricmc.api.EnvType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.Contract;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.util.CheckEnvironment;
import org.ladysnake.cca.api.v8.component.CardinalComponent;
import org.ladysnake.cca.internal.base.ComponentsInternals;

/**
 * A {@link CardinalComponent} implementing this interface will have its data automatically
 * synchronized with players watching its provider.
 *
 * @see ComponentKey#sync(Object)
 */
public interface AutoSyncedComponent extends CardinalComponent, ComponentPacketWriter, PlayerSyncPredicate {

    /**
     * Returns {@code true} if a synchronization packet for this component
     * should be immediately sent to {@code player}.
     *
     * @param player potential recipient of a synchronization packet
     * @return {@code true} if synchronization with the {@code player} should occur,
     * {@code false} otherwise
     */
    @Override
    default boolean shouldSyncWith(ServerPlayer player) {
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
     * specific field to write), or triggering different events on the client (similar to {@link Level#broadcastEntityEvent(Entity, byte)})
     *
     * @param buf       the buffer to write the data to
     * @param recipient the player to which the packet will be sent
     * @implSpec The default implementation writes the whole NBT representation
     * of this component to the buffer using {@link CardinalComponent#writeData(net.minecraft.world.level.storage.ValueOutput)}.
     * @implNote The default implementation should generally be overridden.
     * The serialization done by the default implementation sends possibly hidden
     * information to clients, uses a wasteful data format, and does not support
     * any optimization such as incremental updates. Implementing classes can
     * nearly always provide a better implementation.
     * @see ComponentKey#sync(Object)
     * @see ComponentKey#sync(Object, ComponentPacketWriter)
     * @see #applySyncPacket(RegistryFriendlyByteBuf)
     */
    @Contract(mutates = "param1")
    @Override
    default void writeSyncPacket(RegistryFriendlyByteBuf buf, ServerPlayer recipient) {
        try (var errorReporter = new ProblemReporter.ScopedCollector(ComponentsInternals.LOGGER)) {
            TagValueOutput writeView = TagValueOutput.createWithContext(errorReporter, buf.registryAccess());
            this.writeData(writeView);
            buf.writeNbt(writeView.buildResult());
        }
    }

    /**
     * Reads this component's data from {@code buf}.
     *
     * @implSpec The default implementation converts the buffer's content
     * to a {@link CompoundTag} and calls {@link CardinalComponent#readData(net.minecraft.world.level.storage.ValueInput)}.
     * @implNote any implementing class overriding {@link #writeSyncPacket(RegistryFriendlyByteBuf, ServerPlayer)}
     * such that it uses a different data format must override this method.
     * @see #writeSyncPacket(RegistryFriendlyByteBuf, ServerPlayer)
     */
    @CheckEnvironment(EnvType.CLIENT)
    default void applySyncPacket(RegistryFriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        if (tag != null) {
            try (var errorReporter = new ProblemReporter.ScopedCollector(ComponentsInternals.LOGGER)) {
                this.readData(TagValueInput.create(errorReporter, buf.registryAccess(), tag));
            }
        }
    }
}
