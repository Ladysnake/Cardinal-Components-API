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
package dev.onyxstudios.cca.mixin.item.common;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.internal.base.AbstractComponentContainer;
import dev.onyxstudios.cca.internal.item.CardinalItemInternals;
import dev.onyxstudios.cca.internal.item.InternalStackComponentProvider;
import dev.onyxstudios.cca.internal.item.ItemCaller;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemStack.class)
public abstract class MixinItemStack implements InternalStackComponentProvider {
    @Unique
    private static final ComponentContainer EMPTY_COMPONENTS = ComponentContainer.EMPTY;

    @Unique
    private @Nullable ComponentContainer components;
    @Unique
    private @Nullable CompoundTag serializedComponents;

    @Inject(method = "areTagsEqual", at = @At("RETURN"), cancellable = true)
    private static void areTagsEqual(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        // If the tags are equal, either both stacks are empty or neither is.
        if(cir.getReturnValueZ() && CardinalItemInternals.areComponentsIncompatible(stack1, stack2)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isEqual", at = @At("RETURN"), cancellable = true)
    private void isEqual(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // If the tags are equal, either both stacks are empty or neither is.
        if(cir.getReturnValueZ() && CardinalItemInternals.areComponentsIncompatible((ItemStack) (Object) this, stack)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "copy", at = @At("RETURN"))
    private void copy(CallbackInfoReturnable<ItemStack> cir) {
        CardinalItemInternals.copyComponents(((ItemStack)(Object) this), cir.getReturnValue());
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void serialize(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.components != null) {
            this.components.toTag(cir.getReturnValue());
        } else if (this.serializedComponents != null) {
            CardinalItemInternals.markSharedTag(this.serializedComponents);
            cir.getReturnValue().put(AbstractComponentContainer.NBT_KEY, this.serializedComponents.get(AbstractComponentContainer.NBT_KEY));
        }
    }

    @Shadow
    public abstract Item getItem();

    @Shadow
    private boolean empty;

    @Shadow @Nullable public abstract CompoundTag getTag();

    @Shadow public abstract CompoundTag getOrCreateTag();

    @Shadow public abstract void removeSubTag(String key);

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void initComponentsNBT(CompoundTag tag, CallbackInfo ci) {
        // Keep data without deserializing
        Tag componentData = tag.get(AbstractComponentContainer.NBT_KEY);
        if (componentData != null) {
            CompoundTag serializedComponents = new CompoundTag();
            // the vanilla tag is not copied, so we don't copy our data either
            serializedComponents.put(AbstractComponentContainer.NBT_KEY, componentData);
            this.serializedComponents = serializedComponents;
        }
    }

    @Override
    public ComponentContainer getComponentContainer() {
        if (this.empty) return EMPTY_COMPONENTS;
        if (this.components == null) {
            this.components = ((ItemCaller) this.getItem()).cardinal_createComponents((ItemStack) (Object) this);
            if (this.serializedComponents != null) {
                // If the tag is shared, we need to copy it before deserialization
                // Components may keep direct references to the serialized data, especially in the case of inventories
                this.components.fromTag(CardinalItemInternals.copyIfNeeded(this.serializedComponents));
                this.serializedComponents = null;
            }
        }
        return this.components;
    }

    @Override
    public @Nullable ComponentContainer getActualComponentContainer() {
        return this.components;
    }

    @Override
    public @Nullable CompoundTag cca_getSerializedComponentData() {
        return this.serializedComponents;
    }

    @Override
    public void cca_setSerializedComponentData(@Nullable CompoundTag components) {
        this.serializedComponents = components;
    }
}
