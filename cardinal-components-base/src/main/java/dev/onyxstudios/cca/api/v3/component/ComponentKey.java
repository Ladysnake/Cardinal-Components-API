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

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.sync.ComponentPacketWriter;
import dev.onyxstudios.cca.api.v3.component.sync.PlayerSyncPredicate;
import dev.onyxstudios.cca.internal.base.asm.CcaBootstrap;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * A key for retrieving {@link Component} instances from component providers.
 *
 * <p> A {@link ComponentKey} must be registered for every component type through
 * {@link ComponentRegistryV3#getOrCreate(Identifier, Class)}.
 *
 * @see ComponentRegistryV3
 */
@ApiStatus.NonExtendable
public abstract class ComponentKey<C extends Component> {

    public final Identifier getId() {
        return this.id;
    }

    public final Class<C> getComponentClass() {
        return this.componentClass;
    }

    /**
     * @param provider a component provider
     * @return the attached component associated with this key, or
     * {@code null} if the provider does not support this type of component
     * @throws ClassCastException if the {@code provider} is
     * @see #get(Object)
     * @see #maybeGet(Object)
     */
    @Contract(pure = true)
    public <V> @Nullable C getNullable(V provider) {
        return this.getInternal((ComponentProvider) provider);
    }

    /**
     * @param provider a component provider
     * @param <V>      the class of the component provider
     * @return the nonnull value of the held component of this type
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see #maybeGet(Object)
     */
    public final <V> C get(V provider) {
        C component = this.getInternal((ComponentProvider) provider);
        assert component == null || this.getComponentClass().isInstance(component);
        if (component == null) {
            try {
                throw new NoSuchElementException(provider + " provides no component of type " + this.getId());
            } catch (NullPointerException e) {  // some toString implementations crash in init because the name is not set
                NoSuchElementException e1 = new NoSuchElementException(this.getId() + " not available");
                e1.addSuppressed(e);
                throw e1;
            }
        }
        return component;
    }

    /**
     * @param provider a component provider
     * @param <V>      the class of the component provider
     * @return an {@code Optional} describing a component of this type, or an empty
     * {@code Optional} if {@code provider} does not have such a component.
     * @see #get(Object)
     */
    public final <V> Optional<C> maybeGet(@Nullable V provider) {
        if (provider instanceof ComponentProvider) {
            return Optional.ofNullable(this.getInternal((ComponentProvider) provider));
        }
        return Optional.empty();
    }

    @Contract(pure = true)
    public <V> boolean isProvidedBy(V provider) {
        return this.getNullable(provider) != null;
    }

    /**
     * Attempts to synchronize the component attached to the given provider.
     *
     * <p>This method has no visible effect if the given provider does not support synchronization, or
     * the associated component does not implement an adequate synchronization interface.
     *
     * @param provider a component provider
     * @param <V>      the class of the component provider
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     */
    public <V> void sync(V provider) {
        C c = this.get(provider);
        if (c instanceof AutoSyncedComponent) {
            AutoSyncedComponent synced = (AutoSyncedComponent) c;
            this.sync(provider, synced, synced);
        }
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public <V> void sync(V provider, int syncOp) {
        C c = this.get(provider);
        if (c instanceof dev.onyxstudios.cca.api.v3.component.AutoSyncedComponent) {
            dev.onyxstudios.cca.api.v3.component.AutoSyncedComponent synced = (dev.onyxstudios.cca.api.v3.component.AutoSyncedComponent) c;
            this.sync(provider, (buf, recipient) -> synced.writeToPacket(buf, recipient, syncOp), p -> synced.shouldSyncWith(p, syncOp));
        } else {
            throw new IllegalStateException("syncOp parameter supplied despite absence of legacy synced component");
        }
    }

    /**
     * Attempts to synchronize the component attached to the given provider.
     *
     * <p>This method has no visible effect if the given provider does not support synchronization, or
     * the associated component does not implement an adequate synchronization interface.
     *
     * @param <V>          the class of the component provider
     * @param provider     a component provider
     * @param packetWriter a writer for the sync packet
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     */
    public <V> void sync(V provider, ComponentPacketWriter packetWriter) {
        C c = this.get(provider);
        if (c instanceof AutoSyncedComponent) {
            this.sync(provider, packetWriter, (AutoSyncedComponent) c);
        }
    }

    /**
     * Attempts to synchronize the component attached to the given provider.
     *
     * <p>This method has no visible effect if the given provider does not support synchronization, or
     * the associated component does not implement an adequate synchronization interface.
     *
     * @param <V>          the class of the component provider
     * @param provider     a component provider
     * @param packetWriter a writer for the sync packet
     * @param predicate    a predicate for which players should receive the packet
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     */
    public <V> void sync(V provider, ComponentPacketWriter packetWriter, PlayerSyncPredicate predicate) {
        ComponentProvider prov = (ComponentProvider) provider;
        for (Iterator<ServerPlayerEntity> it = prov.getRecipientsForComponentSync(); it.hasNext();) {
            this.syncWith(it.next(), prov, packetWriter, predicate);
        }
    }

    /**
     * Attempts to synchronize the component attached to the given provider with the given {@code player}.
     *
     * <p>This method has no visible effect if the given provider does not support synchronization, or
     * the associated component does not implement an adequate synchronization interface.
     *
     * @param player   the player with which the component should be synchronized
     * @param provider a component provider
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     */
    @ApiStatus.Experimental
    public void syncWith(ServerPlayerEntity player, ComponentProvider provider) {
        C c = this.get(provider);
        if (c instanceof AutoSyncedComponent) {
            AutoSyncedComponent synced = (AutoSyncedComponent) c;
            this.syncWith(player, provider, synced, synced);
        }
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + "[\"" + this.id + "\"]";
    }

    /* ------------ internal members ------------- */


    private final Identifier id;
    private final Class<C> componentClass;

    /**
     * Constructs a new immutable ComponentType
     *
     * @see ComponentRegistryV3#getOrCreate(Identifier, Class)
     */
    @ApiStatus.Internal
    protected ComponentKey(Identifier id, Class<C> componentClass) {
        if (!CcaBootstrap.INSTANCE.isGenerated(this.getClass())) throw new IllegalStateException();
        this.componentClass = componentClass;
        this.id = id;
    }

    /**
     * @param container a component container
     * @return the attached component of this type, or
     * {@code null} if the container does not support this type of component
     * @see #get(Object)
     * @see #maybeGet(Object)
     */
    // overridden by generated types
    @Contract(pure = true)
    @ApiStatus.Internal
    public abstract @Nullable C getInternal(ComponentContainer container);

    private @Nullable C getInternal(ComponentProvider provider) {
        return this.getInternal(provider.getComponentContainer());
    }

    @ApiStatus.Internal
    public C getFromContainer(ComponentContainer container) {
        return Objects.requireNonNull(this.getInternal(container));
    }


    @ApiStatus.Internal
    public void syncWith(ServerPlayerEntity player, ComponentProvider provider, ComponentPacketWriter writer, PlayerSyncPredicate predicate) {
        if (predicate.shouldSyncWith(player)) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            Packet<?> packet = provider.toComponentPacket(buf, this, writer, player);

            if (packet != null) {
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packet);
            }
        }
    }
}
