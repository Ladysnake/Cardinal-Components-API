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
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import dev.onyxstudios.cca.internal.item.CardinalItemInternals;
import dev.onyxstudios.cca.internal.item.InternalStackComponentProvider;
import dev.onyxstudios.cca.internal.item.ItemCaller;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Mixin(value = ItemStack.class)
public abstract class MixinItemStack implements InternalStackComponentProvider {
    @Unique
    private static final ComponentContainer EMPTY_COMPONENTS = StaticComponentPluginBase.createEmptyContainer("EmptyItemImpl");

    private ComponentContainer components;

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
        this.components.toTag(cir.getReturnValue());
    }

    @Shadow
    public abstract Item getItem();

    /**
     * Direct reference to the item held by this {@code ItemStack}.
     *
     * <p> When inserting an item into an inventory, Minecraft creates
     * an empty itemstack of the right item and then increases the count.
     * ItemStack#getItem() returns the wrong item in those cases,
     * causing component initialization to fail.
     *
     * <p> This is normally deprecated, but we have to use it for the reason
     * above.
     */
    @Shadow
    @Final
    private Item item;

    @Shadow
    private boolean empty;

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("RETURN"))
    private void initComponents(ItemConvertible item, int amount, CallbackInfo ci) {
        this.initComponents();
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void initComponentsNBT(CompoundTag tag, CallbackInfo ci) {
        this.initComponents();
        this.components.fromTag(tag);
    }

    @Unique
    private void initComponents() {
        // we use the actual item type held by this stack, bypassing empty checks made by ItemStack#getItem(),
        // so as to avoid uninitialized components from empty stacks.
        this.components = ((ItemCaller) (this.item == null ? Items.AIR : this.item)).cardinal_createComponents((ItemStack) (Object) this);
    }

    @Nonnull
    @Override
    public ComponentContainer getComponentContainer() {
        return this.empty ? EMPTY_COMPONENTS : this.components;
    }

    @Nonnull
    @Override
    public ComponentContainer getActualComponentContainer() {
        return this.components;
    }
}
