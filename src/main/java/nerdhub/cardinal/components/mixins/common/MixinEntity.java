package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.EntityComponentProvider;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentAccessor;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.util.ComponentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@Mixin(Entity.class)
public abstract class MixinEntity implements EntityComponentProvider, ComponentAccessor {

    private Map<ComponentType<? extends Component>, Component> components = new IdentityHashMap<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initDataTracker(CallbackInfo ci) {
        this.createComponents(this.components);
        EntityComponentCallback.EVENT.invoker().attachComponents((Entity) (Object) this, this.components);
    }

    @Override
    public void createComponents(Map<ComponentType<? extends Component>, Component> components) {
        //NO-OP, but needs to be implemented
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void toTag(CompoundTag inputTag, CallbackInfoReturnable<CompoundTag> cir) {
        ComponentHelper.writeToTag(this.components, cir.getReturnValue());
    }

    @Inject(method = "fromTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V", shift = At.Shift.AFTER))
    private void fromTag(CompoundTag tag, CallbackInfo ci) {
        ComponentHelper.readFromTag(this.components, tag);
    }

    @Override
    public boolean hasComponent(ComponentType<?> type) {
        return this.components.containsKey(type);
    }

    @Nullable
    @Override
    public Component getComponent(ComponentType<?> type) {
        return this.components.getOrDefault(type, null);
    }

    @Override
    public Set<ComponentType<? extends Component>> getComponentTypes() {
        return Collections.unmodifiableSet(this.components.keySet());
    }
}
