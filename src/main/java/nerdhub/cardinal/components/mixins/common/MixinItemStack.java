package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.util.Components;
import nerdhub.cardinal.components.internal.ItemCaller;
import nerdhub.cardinal.components.internal.ItemStackAccessor;
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
import java.util.Set;

@Mixin(value = ItemStack.class, priority = 900)
public abstract class MixinItemStack implements ComponentProvider, ItemStackAccessor {

    private ComponentContainer components;

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

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "copy", at = @At("RETURN"))
    private void copy(CallbackInfoReturnable<ItemStack> cir) {
        ComponentContainer other = ((ItemStackAccessor) (Object) cir.getReturnValue()).cardinal_getComponentContainer();
        this.components.forEach((type, component) -> {
            Component copy = Components.copyOf(component);
            other.put(type, copy);
        });
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void serialize(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        this.components.toTag(cir.getReturnValue());
    }

    @Shadow
    public abstract Item getItem();

    @Shadow public abstract boolean isEmpty();

    @SuppressWarnings({"DuplicatedCode", "ConstantConditions"})
    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("RETURN"))
    private void initComponents(ItemConvertible item, int amount, CallbackInfo ci) {
        this.components = ((ItemCaller) this.getItem()).cardinal_createComponents((ItemStack) (Object) this);
    }

    @SuppressWarnings({"DuplicatedCode", "ConstantConditions"})
    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void initComponentsNBT(CompoundTag tag, CallbackInfo ci) {
        this.components = ((ItemCaller) this.getItem()).cardinal_createComponents((ItemStack) (Object) this);
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
    public ComponentContainer cardinal_getComponentContainer() {
        return this.components;
    }
}
