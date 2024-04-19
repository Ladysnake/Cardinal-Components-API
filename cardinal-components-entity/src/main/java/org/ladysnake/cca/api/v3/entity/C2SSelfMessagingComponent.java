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
package org.ladysnake.cca.api.v3.entity;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import com.mojang.datafixers.util.Unit;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.RegistryByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.sync.C2SComponentPacketWriter;
import org.ladysnake.cca.internal.base.ComponentUpdatePayload;
import org.ladysnake.cca.internal.entity.CardinalComponentsEntity;

import java.util.Objects;

/**
 * A component that is attached to players and that can send messages from the client to the serverside version of itself
 * @since 6.0.0
 */
@ApiStatus.Experimental
public interface C2SSelfMessagingComponent extends Component {
    /**
     * Handles a message sent through this component's channel by the player to which this component is attached
     *
     * <p>Mods should make sure all preconditions are considered when applying changes based on the message - e.g. "is the player an operator" and/or "does the player have the right menu opened".
     *
     * @param buf the buffer containing the data as written in {@link #sendC2SMessage}
     */
    void handleC2SMessage(RegistryByteBuf buf);

    /**
     * Produces and sends a C2S update packet using the given information
     *
     * @param writer a {@link C2SComponentPacketWriter} writing the component's data to the packet
     * @implNote the default implementation should generally be overridden.
     * It finds the component key through a reverse component lookup on the client's main player object,
     * which is inefficient and may even produce incorrect results in some edge cases.
     * @see #sendC2SMessage(ComponentKey, C2SComponentPacketWriter)
     */
    @CheckEnv(Env.CLIENT)
    default void sendC2SMessage(C2SComponentPacketWriter writer) {
        ComponentProvider provider = (ComponentProvider) Objects.requireNonNull(MinecraftClient.getInstance().player);
        ComponentKey<?> key = Objects.requireNonNull(provider.getComponentContainer().getKey(this));
        sendC2SMessage(key, writer);
    }

    /**
     * Produces and sends a C2S update packet using the given information
     *
     * @param key the key describing the component being sent an update
     * @param writer a {@link C2SComponentPacketWriter} writing the component's data to the packet
     */
    @CheckEnv(Env.CLIENT)
    static void sendC2SMessage(ComponentKey<?> key, C2SComponentPacketWriter writer) {
        PacketSender sender = ClientPlayNetworking.getSender(); // checks that the player is in game
        RegistryByteBuf buf = new RegistryByteBuf(PacketByteBufs.create(), Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getRegistryManager());
        buf.writeIdentifier(key.getId());
        writer.writeC2SPacket(buf);
        sender.sendPacket(new ComponentUpdatePayload<>(CardinalComponentsEntity.C2S_SELF_PACKET_ID, Unit.INSTANCE, true, key.getId(), buf), PacketCallbacks.always(buf::release));
    }
}
