/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 Ladysnake
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
package org.ladysnake.componenttest.content.vita;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.DataComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.TransientComponent;
import org.ladysnake.cca.test.base.Vita;

public class ItemVita implements Vita, TransientComponent {
    public static final DataComponentType<ItemVita.Data> COMPONENT_TYPE = DataComponentType.<ItemVita.Data>builder()
        .codec(Data.CODEC)
        .packetCodec(Data.PACKET_CODEC)
        .build();

    public static final DataComponentType<ItemVita.Data> ALT_COMPONENT_TYPE = DataComponentType.<ItemVita.Data>builder()
        .codec(Data.CODEC)
        .packetCodec(Data.PACKET_CODEC)
        .build();

    private final DataComponentType<ItemVita.Data> componentType;
    private final ItemStack stack;

    public ItemVita(DataComponentType<ItemVita.Data> componentType, ItemStack stack) {
        this.componentType = componentType;
        this.stack = stack;
    }

    @Override
    public int getVitality() {
        return this.stack.getOrDefault(this.componentType, Data.EMPTY).vitality();
    }

    @Override
    public void setVitality(int value) {
        this.stack.set(this.componentType, new Data(value));
    }

    public record Data(int vitality) {
        public static final Data EMPTY = new Data(0);
        public static final Codec<Data> CODEC = Codec.INT.xmap(Data::new, Data::vitality);
        public static final PacketCodec<ByteBuf, Data> PACKET_CODEC = PacketCodecs.INTEGER.xmap(Data::new, Data::vitality);
    }
}
