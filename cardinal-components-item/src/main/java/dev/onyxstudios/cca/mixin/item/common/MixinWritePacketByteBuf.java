/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.cca.mixin.item.common;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.internal.item.CardinalItemInternals;
import dev.onyxstudios.cca.internal.item.InternalStackComponentProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import javax.annotation.Nullable;

@Mixin(PacketByteBuf.class)
public abstract class MixinWritePacketByteBuf {
    @Nullable
    @ModifyVariable(method = "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/network/PacketByteBuf;", at = @At(value = "LOAD"))
    private NbtCompound writeItemStack(@Nullable NbtCompound tag, ItemStack stack) {
        InternalStackComponentProvider provider = InternalStackComponentProvider.get(stack);
        NbtCompound serializedComponents;
        NbtCompound frozenData = provider.cca_getSerializedComponentData();

        if (frozenData == null) {
            ComponentContainer componentContainer = provider.getActualComponentContainer();

            if (componentContainer == null || componentContainer.keys().isEmpty()) {
                return tag;
            }

            serializedComponents = componentContainer.toTag(new NbtCompound());
        } else {
            serializedComponents = frozenData;
        }

        NbtCompound newTag = tag == null ? new NbtCompound() : tag.copy();
        newTag.put(CardinalItemInternals.CCA_SYNCED_COMPONENTS, serializedComponents);
        return newTag;
    }
}
