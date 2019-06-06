package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.ItemComponentProvider;
import nerdhub.cardinal.components.api.accessor.StackComponentAccessor;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.util.ComponentHelper;
import nerdhub.cardinal.components.util.accessor.ItemstackComponents;
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
public abstract class MixinItemStack implements StackComponentAccessor, ItemstackComponents {

    private final Map<ComponentType<? extends Component>, Component> components = new IdentityHashMap<>();

    @Inject(method = "areTagsEqual", at = @At("RETURN"), cancellable = true)
    private static void areTagsEqual(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValueZ() && !ComponentHelper.areComponentsEqual(stack1, stack2)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isEqual", at = @At("RETURN"), cancellable = true)
    private void isEqual(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValueZ() && !ComponentHelper.areComponentsEqual((ItemStack) (Object) this, stack)) {
            cir.setReturnValue(false);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "copy", at = @At("RETURN"))
    private void copy(CallbackInfoReturnable<ItemStack> cir) {
        ItemstackComponents other = (ItemstackComponents) (Object) cir.getReturnValue();
        this.components.forEach((type, component) -> {
            Component copy = ComponentHelper.copyOf(component);
            other.setComponentValue(type, copy);
        });
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void serialize(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        ComponentHelper.writeToTag(components, cir.getReturnValue());
    }

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("RETURN"))
    private void initComponents(ItemConvertible item, int amount, CallbackInfo ci) {
        ((ItemComponentProvider) this.getItem()).createComponents((ItemStack) (Object) this);
    }

    @Shadow
    public abstract Item getItem();

    @SuppressWarnings({"unchecked", "ConstantConditions", "DuplicatedCode"})
    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void initComponentsNBT(CompoundTag tag, CallbackInfo ci) {
        ((ItemComponentProvider) this.getItem()).createComponents((ItemStack) (Object) this);
        ComponentHelper.readFromTag(this.components, tag);
    }

    @Override
    public <T extends Component> boolean hasComponent(ComponentType<T> type) {
        return components.containsKey(type);
    }

    @Nullable
    @Override
    public <T extends Component> T getComponent(ComponentType<T> type) {
        return components.containsKey(type) ? type.cast(components.get(type)) : null;
    }

    @Override
    public Set<ComponentType<? extends Component>> getComponentTypes() {
        return Collections.unmodifiableSet(components.keySet());
    }

    @Override
    public <T extends Component> void setComponentValue(ComponentType<T> type, Component obj) {
        components.put(type, obj);
    }
}
