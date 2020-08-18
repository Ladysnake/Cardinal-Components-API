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

import dev.onyxstudios.cca.internal.base.asm.CcaBootstrap;
import io.netty.buffer.Unpooled;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * A key for retrieving {@link Component} instances from component providers.
 *
 * <p> A {@link ComponentKey} must be registered for every component type through
 * {@link ComponentRegistry#registerIfAbsent(Identifier, Class)}.
 *
 * @see ComponentRegistry
 */
@ApiStatus.Experimental
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
    // overridden by generated types
    @ApiStatus.Experimental
    public abstract <V> @Nullable C getNullable(V provider);

    /**
     * @param provider a component provider
     * @param <V>      the class of the component provider
     * @return the nonnull value of the component associated with this key
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see #maybeGet(Object)
     */
    public abstract <V> C get(V provider);

    /**
     * @param provider a component provider
     * @param <V>      the class of the component provider
     * @return an {@code Optional} describing a component associated with this key, or an empty
     * {@code Optional} if {@code provider} does not have such a component.
     * @see #get(Object)
     */
    public abstract <V> Optional<C> maybeGet(@Nullable V provider);

    @ApiStatus.Experimental
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
    @ApiStatus.Experimental
    public <V> void sync(V provider) {
        ComponentProvider prov = (ComponentProvider) provider;
        C c = this.get(prov);

        if (c instanceof AutoSyncedComponent) {
            prov.getRecipientsForComponentSync().forEachRemaining(player -> this.syncWith(player, prov));
        } else if (c instanceof SyncedComponent) {
            ((SyncedComponent) c).sync();
        }
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + "[\"" + this.id + "\"]";
    }

    /* ------------ internal members ------------- */

    private final Class<C> componentClass;
    private final Identifier id;

    /**
     * Constructs a new immutable ComponentType
     *
     * @see ComponentRegistry#registerIfAbsent(Identifier, Class)
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
    @ApiStatus.Internal
    public abstract @Nullable C getInternal(ComponentContainer container);

    @ApiStatus.Internal
    public C getFromContainer(ComponentContainer container) {
        return Objects.requireNonNull(this.getInternal(container));
    }

    @ApiStatus.Internal
    public void syncWith(ServerPlayerEntity player, ComponentProvider provider) {
        C c = this.get(provider);

        if (c instanceof AutoSyncedComponent && ((AutoSyncedComponent) c).shouldSyncWith(player)) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            // can't cast to (C & AutoSyncedComponent), but the guarantees are there
            @SuppressWarnings({"unchecked", "rawtypes"}) Packet<?> packet = provider.toComponentPacket(buf, (ComponentKey) this, (AutoSyncedComponent) c, player);

            if (packet != null) {
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packet);
            }
        } else if (c instanceof SyncedComponent) {
            ((SyncedComponent) c).syncWith(player);
        }
    }
}
