package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.ItemComponentProvider;
import nerdhub.cardinal.components.api.accessor.StackComponentAccessor;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.mixins.accessor.ItemstackComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
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
        if(cir.getReturnValueZ() && !compare(stack1, stack2)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * private helper method
     */
    @SuppressWarnings("ConstantConditions")
    private static boolean compare(ItemStack stack1, ItemStack stack2) {
        if(stack1.isEmpty() && stack2.isEmpty()) {
            return true;
        }
        StackComponentAccessor accessor = (StackComponentAccessor) (Object) stack1;
        StackComponentAccessor other = (StackComponentAccessor) (Object) stack2;
        Set<ComponentType<? extends Component>> types = accessor.getComponentTypes();
        if(types.size() == other.getComponentTypes().size()) {
            for(ComponentType<? extends Component> type : types) {
                if(!other.hasComponent(type) || !accessor.getComponent(type).isComponentEqual(other.getComponent(type))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Inject(method = "isEqual", at = @At("RETURN"), cancellable = true)
    private void isEqual(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValueZ() && !compare((ItemStack) (Object) this, stack)) {
            cir.setReturnValue(false);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "copy", at = @At("RETURN"))
    private void copy(CallbackInfoReturnable<ItemStack> cir) {
        ItemstackComponents other = (ItemstackComponents) (Object) cir.getReturnValue();
        this.components.forEach((type, component) -> {
            Component copy = copyOf(component);
            copy.fromItemTag(component.toItemTag(new CompoundTag()));
            other.setComponentValue(type, copy);
        });
    }

    /**
     * private helper method
     */
    private static Component copyOf(Component toCopy) {
        Component ret = toCopy.newInstanceForItemStack();
        ret.fromItemTag(toCopy.toItemTag(new CompoundTag()));
        return ret;
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void serialize(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if(!components.isEmpty()) {
            ListTag componentList = new ListTag();
            components.forEach((type, component) -> {
                CompoundTag componentTag = new CompoundTag();
                componentTag.putString("id", type.getID().toString());
                componentTag.put("component", component.toItemTag(new CompoundTag()));
                componentList.add(componentTag);
            });
            cir.getReturnValue().put("cardinal_components", componentList);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("RETURN"))
    private void initComponents(ItemConvertible item, int amount, CallbackInfo ci) {
        ((ItemComponentProvider) this.getItem()).createComponents((ItemStack) (Object) this);
    }

    @Shadow
    public abstract Item getItem();

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void initComponentsNBT(CompoundTag tag, CallbackInfo ci) {
        ((ItemComponentProvider) this.getItem()).createComponents((ItemStack) (Object) this);
        if(tag.containsKey("cardinal_components", 9)) {
            ListTag componentList = tag.getList("cardinal_components", 10);
            componentList.stream().map(CompoundTag.class::cast).forEach(data -> {
                ComponentType type = ComponentRegistry.get(new Identifier(data.getString("id")));
                if(this.hasComponent(type)) {
                    this.getComponent(type).fromItemTag(data.getCompound("component"));
                }
            });
        }
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
