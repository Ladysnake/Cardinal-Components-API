package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.ItemComponentProvider;
import nerdhub.cardinal.components.api.accessor.StackComponentAccessor;
import nerdhub.cardinal.components.api.component.ItemComponent;
import nerdhub.cardinal.components.mixins.accessor.ItemstackComponents;
import net.minecraft.item.ItemProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements StackComponentAccessor, ItemstackComponents {

    private final Map<ComponentType<?>, ItemComponent> components = new IdentityHashMap<>();

    @Inject(method = "<init>(Lnet/minecraft/item/ItemProvider;I)V", at = @At("RETURN"))
    private void initComponents(ItemProvider item, int amount, CallbackInfo ci) {
        ItemStack stack = ((ItemStack) (Object) this);
        ((ItemComponentProvider) stack.getItem()).initComponents(stack);
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void initComponentsNBT(CompoundTag tag, CallbackInfo ci) {
        ItemStack stack = ((ItemStack) (Object) this);
        ((ItemComponentProvider) stack.getItem()).initComponents(stack);
    }

    @Override
    public <T extends ItemComponent> void addComponent(ComponentType<T> type, T obj) {
        components.put(type, obj);
    }

    @Override
    public <T extends ItemComponent> boolean hasComponent(ComponentType<T> type) {
        return components.containsKey(type);
    }

    @Nullable
    @Override
    public <T extends ItemComponent> T getComponent(ComponentType<T> type) {
        return components.containsKey(type) ? type.cast(components.get(type)) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<ComponentType<? extends ItemComponent>> getComponentTypes(ItemStack stack) {
        return Collections.unmodifiableSet((Set<ComponentType<? extends ItemComponent>>) components.keySet());
    }
}
