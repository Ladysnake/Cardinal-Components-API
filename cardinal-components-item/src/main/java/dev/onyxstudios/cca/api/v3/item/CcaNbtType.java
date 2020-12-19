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
package dev.onyxstudios.cca.api.v3.item;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.*;

@SuppressWarnings("unused")
public final class CcaNbtType<T extends Tag> {
    public static final CcaNbtType<ByteTag> BYTE = new CcaNbtType<>(NbtType.BYTE);
    public static final CcaNbtType<ShortTag> SHORT = new CcaNbtType<>(NbtType.SHORT);
    public static final CcaNbtType<IntTag> INT = new CcaNbtType<>(NbtType.INT);
    public static final CcaNbtType<LongTag> LONG = new CcaNbtType<>(NbtType.LONG);
    public static final CcaNbtType<FloatTag> FLOAT = new CcaNbtType<>(NbtType.FLOAT);
    public static final CcaNbtType<DoubleTag> DOUBLE = new CcaNbtType<>(NbtType.DOUBLE);
    public static final CcaNbtType<ByteArrayTag> BYTE_ARRAY = new CcaNbtType<>(NbtType.BYTE_ARRAY);
    public static final CcaNbtType<StringTag> STRING = new CcaNbtType<>(NbtType.STRING);
    public static final CcaNbtType<ListTag> LIST = new CcaNbtType<>(NbtType.LIST);
    public static final CcaNbtType<CompoundTag> COMPOUND = new CcaNbtType<>(NbtType.COMPOUND);
    public static final CcaNbtType<IntArrayTag> INT_ARRAY = new CcaNbtType<>(NbtType.INT_ARRAY);
    public static final CcaNbtType<LongArrayTag> LONG_ARRAY = new CcaNbtType<>(NbtType.LONG_ARRAY);

    private final int type;

    private CcaNbtType(int type) {
        this.type = type;
    }

    public int getId() {
        return this.type;
    }
}
