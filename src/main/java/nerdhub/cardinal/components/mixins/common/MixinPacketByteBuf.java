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
