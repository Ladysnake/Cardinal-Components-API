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
package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.util.Components;
import nerdhub.cardinal.components.internal.ItemCaller;
import nerdhub.cardinal.components.internal.ItemStackAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

@Mixin(value = ItemStack.class, priority = 900)
public abstract class MixinItemStack implements ComponentProvider, ItemStackAccessor {

    private ComponentContainer<CopyableComponent<?>> components;

    @Inject(method = "areTagsEqual", at = @At("RETURN"), cancellable = true)
    private static void areTagsEqual(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        // If the tags are equal, either both stacks are empty or neither is.
        if(cir.getReturnValueZ() && !Components.areComponentsEqual(stack1, stack2)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isEqualIgnoreDamage", at = @At("RETURN"), cancellable = true)
    private void isEqual(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // If the tags are equal, either both stacks are empty or neither is.
        if(cir.getReturnValueZ() && !Components.areComponentsEqual((ItemStack) (Object) this, stack)) {
            cir.setReturnValue(false);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "copy", at = @At("RETURN"))
    private void copy(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack other = cir.getReturnValue();
        Components.forEach(this, (type, component) -> ((CopyableComponent) type.get(other)).copyFrom(component));
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void serialize(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        this.components.toTag(cir.getReturnValue());
    }

    @Shadow
    public abstract Item getItem();

    @Shadow public abstract boolean isEmpty();

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

    @SuppressWarnings({"DuplicatedCode", "ConstantConditions"})
    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("RETURN"))
    private void initComponents(ItemConvertible item, int amount, CallbackInfo ci) {
        // direct item reference, to avoid uninitialized components from empty stack
        this.components = ((ItemCaller) this.item).cardinal_createComponents((ItemStack) (Object) this);
    }

    @SuppressWarnings({"DuplicatedCode", "ConstantConditions"})
    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void initComponentsNBT(CompoundTag tag, CallbackInfo ci) {
        // direct item reference, to avoid uninitialized components from empty stack
        this.components = ((ItemCaller) this.item).cardinal_createComponents((ItemStack) (Object) this);
        this.components.fromTag(tag);
    }

    @Override
    public boolean hasComponent(ComponentType<?> type) {
        return !this.isEmpty() && components.containsKey(type);
    }

    @Nullable
    @Override
    public <C extends Component> C getComponent(ComponentType<C> type) {
        return this.isEmpty() ? null : components.get(type);
    }

    @Override
    public Set<ComponentType<?>> getComponentTypes() {
        return this.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(components.keySet());
    }

    @Override
    public ComponentContainer<CopyableComponent<?>> cardinal_getComponentContainer() {
        return this.components;
    }
}
