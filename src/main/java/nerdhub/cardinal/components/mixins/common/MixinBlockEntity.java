package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.SidedComponentProvider;
import nerdhub.cardinal.components.impl.SidedComponentProvidingContainer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements SidedComponentProvider {

    @Shadow public abstract BlockEntityType<?> getType();

    @Unique
    private SidedComponentProvidingContainer components = new SidedComponentProvidingContainer();

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void fireComponentCallback(CallbackInfo ci) {
        // Mixin classes can be referenced from other mixin classes
        //noinspection ReferenceToMixin,ConstantConditions
        ((MixinBlockEntityType)(Object)this.getType()).cardinal_fireComponentEvents((BlockEntity) (Object) this, this.components);
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void toTag(CompoundTag inputTag, CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().put("cardinal:components", this.components.toTag(new CompoundTag()));
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void fromTag(CompoundTag tag, CallbackInfo ci) {
        this.components.fromTag(tag.getCompound("cardinal:components"));
    }

    @Override
    public ComponentProvider getComponents(@Nullable Direction side) {
        return this.components.getComponents(side);
    }

}
