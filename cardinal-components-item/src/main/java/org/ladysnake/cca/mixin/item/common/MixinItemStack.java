/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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
package org.ladysnake.cca.mixin.item.common;

import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.item.ItemTagInvalidationListener;
import org.ladysnake.cca.internal.base.AbstractComponentContainer;
import org.ladysnake.cca.internal.item.ItemCaller;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemStack.class)
public abstract class MixinItemStack implements ComponentProvider {
    @Unique
    private static final ComponentContainer EMPTY_COMPONENTS = ComponentContainer.EMPTY;

    @Unique
    private @Nullable ComponentContainer components;

    @Inject(method = "setNbt", at = @At("RETURN"))
    private void invalidateCaches(NbtCompound tag, CallbackInfo ci) {
        ComponentContainer components = this.components;

        if (components != null) {
            for (ComponentKey<?> key : components.keys()) {
                Component c = key.getInternal(components);
                if (c instanceof ItemTagInvalidationListener listener) {
                    listener.onTagInvalidated();
                }
            }
        }
    }

    @Shadow
    public abstract Item getItem();

    @Shadow public abstract boolean isEmpty();

    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("RETURN"))
    private void initComponentsNBT(NbtCompound tag, CallbackInfo ci) {
        // Backwards save compatibility (see ItemComponent#readFromNbt)
        if (tag.contains(AbstractComponentContainer.NBT_KEY)) {
            this.getComponentContainer().fromTag(tag);
        }
    }

    @Override
    public ComponentContainer getComponentContainer() {
        if (this.isEmpty()) return EMPTY_COMPONENTS;
        if (this.components == null) {
            this.components = ((ItemCaller) this.getItem()).cardinal_createComponents((ItemStack) (Object) this);
        }
        return this.components;
    }
}
