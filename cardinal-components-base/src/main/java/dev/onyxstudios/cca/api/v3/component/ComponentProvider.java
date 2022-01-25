/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2022 OnyxStudios
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

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.sync.ComponentPacketWriter;
import dev.onyxstudios.cca.api.v3.component.sync.PlayerSyncPredicate;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.level.LevelProperties;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * used to access an object's components.
 */
public interface ComponentProvider {

    /**
     * Convenience method to retrieve ComponentProvider from a given {@link BlockEntity}
     * Requires the <tt>cardinal-components-block</tt> module.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated(forRemoval = true, since = "4.1.0")
    static ComponentProvider fromBlockEntity(BlockEntity blockEntity) {
        return (ComponentProvider) blockEntity;
    }

    /**
     * Convenience method to retrieve a ComponentProvider from given {@link Chunk}.
     * Requires the <tt>cardinal-components-chunk</tt> module.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated(forRemoval = true, since = "4.1.0")
    static ComponentProvider fromChunk(Chunk chunk) {
        return (ComponentProvider) chunk;
    }

    /**
     * Convenience method to retrieve a ComponentProvider from a given {@link Entity}.
     * Requires the <tt>cardinal-components-entity</tt> module.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated(forRemoval = true, since = "4.1.0")
    static ComponentProvider fromEntity(Entity entity) {
        return (ComponentProvider) entity;
    }

    /**
     * Convenience method to retrieve ComponentProvider from a given {@link ItemStack}
     * Requires the <tt>cardinal-components-item</tt> module.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated(forRemoval = true, since = "4.1.0")
    @SuppressWarnings("ConstantConditions")
    static ComponentProvider fromItemStack(ItemStack stack) {
        return (ComponentProvider) (Object) stack;
    }

    /**
     * Convenience method to retrieve a ComponentProvider from given {@link LevelProperties}.
     * Requires the <tt>cardinal-components-level</tt> module.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated(forRemoval = true, since = "4.1.0")
    static ComponentProvider fromLevel(WorldProperties level) {
        return (ComponentProvider) level;
    }

    /**
     * Convenience method to retrieve a ComponentProvider from given {@link Scoreboard}.
     * Requires the <tt>cardinal-components-scoreboard</tt> module.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated(forRemoval = true, since = "4.1.0")
    static ComponentProvider fromScoreboard(Scoreboard scoreboard) {
        return (ComponentProvider) scoreboard;
    }

    /**
     * Convenience method to retrieve a ComponentProvider from given {@link Team}.
     * Requires the <tt>cardinal-components-scoreboard</tt> module.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated(forRemoval = true, since = "4.1.0")
    static ComponentProvider fromTeam(AbstractTeam team) {
        return (ComponentProvider) team;
    }

    /**
     * Convenience method to retrieve a ComponentProvider from a given {@link World}.
     * Requires the <tt>cardinal-components-world</tt> module.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated(forRemoval = true, since = "4.1.0")
    static ComponentProvider fromWorld(World world) {
        return (ComponentProvider) world;
    }

    /**
     * @return a runtime-generated component container storing statically declared components.
     */
    ComponentContainer getComponentContainer();

    /**
     * @param key the key object for the type of component to retrieve
     * @return the nonnull value of the held component of this type
     * @param <C> the type of component to retrieve
     * @throws NullPointerException if {@code key} is null
     * @throws NoSuchElementException if this provider does not provide the desired type of component
     * @since 4.1.0
     */
    @ApiStatus.Experimental
    default <C extends Component> C getComponent(ComponentKey<C> key) {
        return key.get(this);
    }

    /**
     * Retrieves an iterator describing the list of players who may receive
     * component sync packets from this provider.
     *
     * <p>Individual components can {@linkplain PlayerSyncPredicate#shouldSyncWith(ServerPlayerEntity) filter
     * which players among the returned list should receive their packets};
     * this method does <em>not</em> take current components into account, so callers should perform
     * additional checks as needed.
     *
     * @return a list of player candidates for receiving component sync packets
     */
    default Iterator<ServerPlayerEntity> getRecipientsForComponentSync() {
        // TODO 5.0.0 replace with Iterable
        return Collections.emptyIterator();
    }

    /**
     * Produces a sync packet using the given information.
     *
     * @param key the key describing the component being synchronized
     * @param writer a {@link ComponentPacketWriter} writing the component's data to the packet
     * @param recipient the player receiving the packet
     * @return a {@link net.minecraft.network.Packet} that has all the information required to perform the component sync
     * @since 3.0.0
     */
    @Nullable
    default <C extends AutoSyncedComponent> CustomPayloadS2CPacket toComponentPacket(ComponentKey<? super C> key, ComponentPacketWriter writer, ServerPlayerEntity recipient) {
        // TODO 4.0.0 inline
        return toComponentPacket(PacketByteBufs.create(), key, writer, recipient);
    }

    /**
     * @deprecated override/call {@link #toComponentPacket(ComponentKey, ComponentPacketWriter, ServerPlayerEntity)} instead
     */
    @SuppressWarnings("unused") @Deprecated(since = "3.0.0", forRemoval = true)
    @Nullable
    default <C extends AutoSyncedComponent> CustomPayloadS2CPacket toComponentPacket(PacketByteBuf buf, ComponentKey<? super C> key, ComponentPacketWriter writer, ServerPlayerEntity recipient) {
        return null;
    }
}
