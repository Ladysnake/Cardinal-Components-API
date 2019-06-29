/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
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

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PacketByteBuf.class)
public abstract class MixinPacketByteBuf {

    @Inject(method = "writeItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/PacketByteBuf;writeCompoundTag(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/util/PacketByteBuf;", shift = At.Shift.AFTER), cancellable = true)
    private void writeItemStack(ItemStack stack, CallbackInfoReturnable<PacketByteBuf> cir) {
        ComponentProvider components = ComponentProvider.fromItemStack(stack);
        CompoundTag containerTag = new CompoundTag();
        ListTag tag = new ListTag();
        for (ComponentType<? extends Component> type : components.getComponentTypes()) {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("id", type.getId().toString());
            nbt.put("data", type.get(this).toTag(new CompoundTag()));
            tag.add(nbt);
        }
        containerTag.put("cardinal_components", tag);
        ((PacketByteBuf) (Object) this).writeCompoundTag(containerTag);
    }

    @SuppressWarnings({"ConstantConditions"})
    @Inject(method = "readItemStack", at = @At(value = "RETURN"))
    private void readStack(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if(!stack.isEmpty()) {
            ComponentProvider components = ComponentProvider.fromItemStack(stack);
            CompoundTag containerTag = ((PacketByteBuf) (Object) this).readCompoundTag();
            ListTag componentList = containerTag.getList("cardinal_components", 10);
            for(int i = 0; i < componentList.size(); i++) {
                CompoundTag tag = componentList.getCompoundTag(i);
                ComponentType<?> type = ComponentRegistry.INSTANCE.get(new Identifier(tag.getString("id")));
                components.getComponent(type).fromTag(tag.getCompound("data"));
            }
        }
    }
}
