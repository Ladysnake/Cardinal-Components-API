/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 OnyxStudios
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

import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Unit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.ChunkPos;

import java.util.function.Function;

public final class MorePacketCodecs {
    public static final PacketCodec<ByteBuf, Unit> EMPTY = PacketCodec.unit(Unit.INSTANCE);

    public static final PacketCodec<RegistryByteBuf, RegistryByteBuf> REG_BYTE_BUF = PacketCodec.ofStatic(
        (buf, value) -> {
            buf.writeVarInt(value.readableBytes());
            buf.writeBytes(value);
        },
        (buf) -> {
            int readableBytes = buf.readVarInt();
            ByteBuf copy = Unpooled.buffer(readableBytes, readableBytes);
            buf.readBytes(copy, readableBytes);
            return new RegistryByteBuf(copy, buf.getRegistryManager());
        }
    );

    /**
     * A codec for a {@link ChunkPos}.
     *
     * @see PacketByteBuf#readChunkPos()
     * @see PacketByteBuf#writeChunkPos(ChunkPos)
     */
    public static final PacketCodec<PacketByteBuf, ChunkPos> CHUNKPOS = PacketCodec.ofStatic(
        PacketByteBuf::writeChunkPos,
        PacketByteBuf::readChunkPos
    );

    /**
     * {@return a codec for encoding three values}
     */
    public static <B, C, T1, T2, T3, T4> PacketCodec<B, C> tuple(
        PacketCodec<? super B, T1> codec1,
        Function<C, T1> from1,
        PacketCodec<? super B, T2> codec2,
        Function<C, T2> from2,
        PacketCodec<? super B, T3> codec3,
        Function<C, T3> from3,
        PacketCodec<? super B, T4> codec4,
        Function<C, T4> from4,
        Function4<T1, T2, T3, T4, C> to
    ) {
        return PacketCodec.ofStatic(
            (buf, value) -> {
                codec1.encode(buf, from1.apply(value));
                codec2.encode(buf, from2.apply(value));
                codec3.encode(buf, from3.apply(value));
                codec4.encode(buf, from4.apply(value));
            },
            (buf) -> {
                T1 object2 = codec1.decode(buf);
                T2 object3 = codec2.decode(buf);
                T3 object4 = codec3.decode(buf);
                T4 object5 = codec4.decode(buf);
                return to.apply(object2, object3, object4, object5);
            }
        );
    }
}
