package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.accessor.StackComponentAccessor;
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
    private void writeItemStack(ItemStack itemStack_1, CallbackInfoReturnable<PacketByteBuf> cir) {
        StackComponentAccessor stack = StackComponentAccessor.get(itemStack_1);
        CompoundTag containerTag = new CompoundTag();
        ListTag tag = new ListTag();
        stack.getComponentTypes().forEach(type -> {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("id", type.getId().toString());
            nbt.put("data", stack.getComponent(type).serialize(new CompoundTag()));
            tag.add(nbt);
        });
        containerTag.put("cardinal_components", tag);
        ((PacketByteBuf) (Object) this).writeCompoundTag(containerTag);
    }

    @SuppressWarnings({"ConstantConditions"})
    @Inject(method = "readItemStack", at = @At(value = "RETURN"))
    private void readStack(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack itemStack_1 = cir.getReturnValue();
        if(!itemStack_1.isEmpty()) {
            StackComponentAccessor stack = StackComponentAccessor.get(itemStack_1);
            CompoundTag containerTag = ((PacketByteBuf) (Object) this).readCompoundTag();
            ListTag componentList = containerTag.getList("cardinal_components", 10);
            for(int i = 0; i < componentList.size(); i++) {
                CompoundTag tag = componentList.getCompoundTag(i);
                ComponentType<?> type = ComponentRegistry.get(new Identifier(tag.getString("id")));
                stack.getComponent(type).deserialize(tag.getCompound("data"));
            }
        }
    }
}
