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
package nerdhub.cardinal.components.mixins.common.item;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.InternalComponentProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(PacketByteBuf.class)
public abstract class MixinPacketByteBuf {

    @Nullable
    @ModifyVariable(method = "writeItemStack", at = @At(value = "LOAD"))
    private CompoundTag markModdedStack(@Nullable CompoundTag tag, ItemStack stack) {
        //noinspection ConstantConditions
        ComponentContainer<?> componentContainer = ((InternalComponentProvider) (Object) stack).getComponentContainer();
        if (componentContainer.isEmpty()) {
            return tag;
        }
        CompoundTag newTag = tag == null ? new CompoundTag() : tag.copy();
        newTag.put("cca_synced_components", componentContainer.toTag(new CompoundTag()));
        return newTag;
    }

    @Inject(method = "readItemStack", at = @At(value = "RETURN", ordinal = 1))
    private void readStack(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();

        CompoundTag syncedComponents = stack.getSubTag("cca_synced_components");
        if (syncedComponents != null) {
            //noinspection ConstantConditions
            ((InternalComponentProvider) ((Object) stack)).getComponentContainer().fromTag(syncedComponents);
            stack.removeSubTag("cca_synced_components");
        }
    }
}
