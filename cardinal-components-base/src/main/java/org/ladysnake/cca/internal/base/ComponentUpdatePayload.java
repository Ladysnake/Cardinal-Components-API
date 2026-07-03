/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2026 Ladysnake
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
package org.ladysnake.cca.internal.base;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

import java.util.Optional;

public record ComponentUpdatePayload<T>(
        Type<ComponentUpdatePayload<T>> id,
        T targetData,
        boolean required,
        Identifier componentKeyId,
        byte[] rawPayload
) implements CustomPacketPayload {
    public static <T> CustomPacketPayload.Type<ComponentUpdatePayload<T>> id(String path) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("cardinal-components", path));
    }

    public static <T> void register(Type<ComponentUpdatePayload<T>> id, StreamCodec<? super RegistryFriendlyByteBuf, T> targetDataCodec) {
        PayloadTypeRegistry.clientboundPlay().register(id, codec(id, targetDataCodec));
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, ComponentUpdatePayload<T>> codec(Type<ComponentUpdatePayload<T>> id, StreamCodec<? super RegistryFriendlyByteBuf, T> targetDataCodec) {
        return StreamCodec.composite(
            StreamCodec.unit(id), ComponentUpdatePayload::id,
            targetDataCodec, ComponentUpdatePayload::targetData,
            ByteBufCodecs.BOOL, ComponentUpdatePayload::required,
            Identifier.STREAM_CODEC, ComponentUpdatePayload::componentKeyId,
            ByteBufCodecs.BYTE_ARRAY, ComponentUpdatePayload::rawPayload,
            ComponentUpdatePayload::new
        );
    }

    public Optional<ComponentKey<?>> componentKey() {
        ComponentKey<?> key = ComponentRegistry.get(this.componentKeyId());
        if (key == null && this.required()) {
            throw new UnknownComponentException("Unknown component " + this.componentKeyId());
        }
        return Optional.ofNullable(key);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return id;
    }
}
