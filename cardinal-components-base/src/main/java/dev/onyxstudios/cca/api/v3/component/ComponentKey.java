/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 OnyxStudios
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
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.sync.C2SComponentPacketWriter;
import dev.onyxstudios.cca.api.v3.component.sync.ComponentPacketWriter;
import dev.onyxstudios.cca.api.v3.component.sync.PlayerSyncPredicate;
import dev.onyxstudios.cca.internal.base.asm.CcaBootstrap;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

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
     * Attempts to retrieve a component instance from the given {@code provider}.
     *
     * @param provider a component provider
     * @return the attached component associated with this key, or
     * {@code null} if the provider does not support this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see #get(Object)
     * @see #maybeGet(Object)
     */
    @Contract(pure = true)
    public @Nullable C getNullable(Object provider) {
        return this.getInternal(((ComponentProvider) provider).getComponentContainer());
    }

    /**
     * Retrieves a component instance from the given {@code provider}.
     *
     * @param provider a component provider
     * @return the nonnull value of the held component of this type
     * @throws NullPointerException if {@code provider} is null
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see #maybeGet(Object)
     * @see ComponentAccess#getComponent(ComponentKey)
     */
    public final C get(Object provider) {
        C component = this.getInternal(((ComponentProvider) provider).getComponentContainer());

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
     * @return an {@code Optional} describing a component of this type, or an empty
     * {@code Optional} if {@code provider} does not have such a component.
     * @see #get(Object)
     */
    public final Optional<C> maybeGet(@Nullable Object provider) {
        if (provider instanceof ComponentProvider p) {
            return Optional.ofNullable(this.getInternal(p.getComponentContainer()));
        }
        return Optional.empty();
    }

    @Contract(pure = true)
    public boolean isProvidedBy(Object provider) {
        return this.getNullable(provider) != null;
    }

    /**
     * Attempts to synchronize the component attached to the given provider.
     *
     * <p>This method has no visible effect if the given provider does not support synchronization, or
     * the associated component does not implement an adequate synchronization interface.
     *
     * @param provider a component provider
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see ComponentAccess#syncComponent(ComponentKey)
     */
    public void sync(Object provider) {
        if (this.get(provider) instanceof AutoSyncedComponent synced) {
            this.sync(provider, synced, synced);
        }
    }

    /**
     * Attempts to synchronize the component attached to the given provider.
     *
     * <p>This method has no visible effect if the given provider does not support synchronization, or
     * the associated component does not implement an adequate synchronization interface.
     *
     * @param provider     a component provider
     * @param packetWriter a writer for the sync packet
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see ComponentAccess#syncComponent(ComponentKey, ComponentPacketWriter)
     */
    public void sync(Object provider, ComponentPacketWriter packetWriter) {
        if (this.get(provider) instanceof AutoSyncedComponent synced) {
            this.sync(provider, packetWriter, synced);
        }
    }

    /**
     * Attempts to synchronize the component attached to the given provider.
     *
     * <p>This method has no visible effect if the given provider does not support synchronization, or
     * the associated component does not implement an adequate synchronization interface.
     *
     * @param provider     a component provider
     * @param packetWriter a writer for the sync packet
     * @param predicate    a predicate for which players should receive the packet
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see ComponentAccess#syncComponent(ComponentKey, ComponentPacketWriter, PlayerSyncPredicate)
     */
    public void sync(Object provider, ComponentPacketWriter packetWriter, PlayerSyncPredicate predicate) {
        for (ServerPlayerEntity player : ((ComponentProvider) provider).getRecipientsForComponentSync()) {
            this.syncWith(player, (ComponentProvider) provider, packetWriter, predicate);
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
        if (this.get(provider) instanceof AutoSyncedComponent synced) {
            this.syncWith(player, provider, synced, synced);
        }
    }

    @ApiStatus.Experimental
    public void syncWith(ServerPlayerEntity player, ComponentProvider provider, ComponentPacketWriter writer, PlayerSyncPredicate predicate) {
        if (predicate.shouldSyncWith(player)) {
            Packet<?> packet = provider.toComponentPacket(this, writer, player);

            if (packet != null) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    @CheckEnv(Env.CLIENT)
    public void sendToServer(ComponentProvider provider, C2SComponentPacketWriter writer) {
        provider.sendC2SMessage(this, writer);
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

    @ApiStatus.Internal
    public C getFromContainer(ComponentContainer container) {
        return Objects.requireNonNull(this.getInternal(container));
    }
}
