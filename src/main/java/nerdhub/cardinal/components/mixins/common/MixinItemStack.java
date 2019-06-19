package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.ItemComponentProvider;
import nerdhub.cardinal.components.api.util.ComponentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@Mixin(value = ItemStack.class, priority = 900)
public abstract class MixinItemStack implements ComponentProvider {

    private final Map<ComponentType<? extends Component>, Component> components = new IdentityHashMap<>();

    @Inject(method = "areTagsEqual", at = @At("RETURN"), cancellable = true)
    private static void areTagsEqual(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValueZ() && !ComponentHelper.areComponentsEqual(stack1, stack2)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isEqualIgnoreDamage", at = @At("RETURN"), cancellable = true)
    private void isEqual(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValueZ() && !ComponentHelper.areComponentsEqual((ItemStack) (Object) this, stack)) {
            cir.setReturnValue(false);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "copy", at = @At("RETURN"))
    private void copy(CallbackInfoReturnable<ItemStack> cir) {
        MixinItemStack other = (MixinItemStack) (Object) cir.getReturnValue();
        this.components.forEach((type, component) -> {
            Component copy = ComponentHelper.copyOf(component);
            other.setComponentValue(type, copy);
        });
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void serialize(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        ComponentHelper.writeToTag(components, cir.getReturnValue());
    }

    @Shadow
    public abstract Item getItem();

    @SuppressWarnings("InvalidMemberReference") // Minecraft dev plugin does not support array values
    @Inject(method = {"<init>(Lnet/minecraft/item/ItemConvertible;I)V", "<init>(Lnet/minecraft/nbt/CompoundTag;)V"}, at = @At("RETURN"))
    private void initComponents(ItemConvertible item, int amount, CallbackInfo ci) {
        ((ItemComponentProvider) this.getItem()).createComponents((ItemStack) (Object) this);
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void initComponentsNBT(CompoundTag tag, CallbackInfo ci) {
        ComponentHelper.readFromTag(this.components, tag);
    }

    @Override
    public boolean hasComponent(ComponentType<?> type) {
        return components.containsKey(type);
    }

    @Nullable
    @Override
    public Component getComponent(ComponentType<?> type) {
        return components.getOrDefault(type, null);
    }

    @Override
    public Set<ComponentType<? extends Component>> getComponentTypes() {
        return Collections.unmodifiableSet(components.keySet());
    }

    <T extends Component> void setComponentValue(ComponentType<T> type, Component obj) {
        components.put(type, obj);
    }
}
