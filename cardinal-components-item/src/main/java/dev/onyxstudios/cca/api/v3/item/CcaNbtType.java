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
package dev.onyxstudios.cca.api.v3.item;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;

@SuppressWarnings("unused")
public final class CcaNbtType<T extends NbtElement> {
    public static final CcaNbtType<NbtByte> BYTE = new CcaNbtType<>(NbtType.BYTE);
    public static final CcaNbtType<NbtShort> SHORT = new CcaNbtType<>(NbtType.SHORT);
    public static final CcaNbtType<NbtInt> INT = new CcaNbtType<>(NbtType.INT);
    public static final CcaNbtType<NbtLong> LONG = new CcaNbtType<>(NbtType.LONG);
    public static final CcaNbtType<NbtFloat> FLOAT = new CcaNbtType<>(NbtType.FLOAT);
    public static final CcaNbtType<NbtDouble> DOUBLE = new CcaNbtType<>(NbtType.DOUBLE);
    public static final CcaNbtType<NbtByteArray> BYTE_ARRAY = new CcaNbtType<>(NbtType.BYTE_ARRAY);
    public static final CcaNbtType<NbtString> STRING = new CcaNbtType<>(NbtType.STRING);
    public static final CcaNbtType<NbtList> LIST = new CcaNbtType<>(NbtType.LIST);
    public static final CcaNbtType<NbtCompound> COMPOUND = new CcaNbtType<>(NbtType.COMPOUND);
    public static final CcaNbtType<NbtIntArray> INT_ARRAY = new CcaNbtType<>(NbtType.INT_ARRAY);
    public static final CcaNbtType<NbtLongArray> LONG_ARRAY = new CcaNbtType<>(NbtType.LONG_ARRAY);

    public static CcaNbtType<?> byId(int id) {
        return switch (id) {
            case NbtType.BYTE -> BYTE;
            case NbtType.SHORT -> SHORT;
            case NbtType.INT -> INT;
            case NbtType.LONG -> LONG;
            case NbtType.FLOAT -> FLOAT;
            case NbtType.DOUBLE -> DOUBLE;
            case NbtType.BYTE_ARRAY -> BYTE_ARRAY;
            case NbtType.STRING -> STRING;
            case NbtType.LIST -> LIST;
            case NbtType.COMPOUND -> COMPOUND;
            case NbtType.INT_ARRAY -> INT_ARRAY;
            case NbtType.LONG_ARRAY -> LONG_ARRAY;
            default -> throw new IllegalArgumentException("Unsupported NBT Type " + id);
        };
    }

    private final int type;

    private CcaNbtType(int type) {
        this.type = type;
    }

    public int getId() {
        return this.type;
    }
}
