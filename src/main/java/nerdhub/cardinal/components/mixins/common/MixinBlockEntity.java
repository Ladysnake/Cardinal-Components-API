package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.component.SidedContainerCompound;
import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.SidedProviderCompound;
import nerdhub.cardinal.components.api.util.impl.DirectSidedProviderCompound;
import nerdhub.cardinal.components.api.util.impl.IndexedComponentContainer;
import nerdhub.cardinal.components.api.util.impl.SuppliedSidedContainerCompound;
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
public abstract class MixinBlockEntity implements SidedProviderCompound {

    @Shadow public abstract BlockEntityType<?> getType();

    @Unique
    private final SidedContainerCompound componentContainer = new SuppliedSidedContainerCompound(IndexedComponentContainer::new);
    @Unique
    private DirectSidedProviderCompound components = new DirectSidedProviderCompound(componentContainer);

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void fireComponentCallback(CallbackInfo ci) {
        // TODO attach when the block entity is added to the world, for full context
        // Mixin classes can be referenced from other mixin classes
        //noinspection ReferenceToMixin,ConstantConditions
        ((MixinBlockEntityType)(Object)this.getType()).cardinal_fireComponentEvents((BlockEntity) (Object) this, this.componentContainer);
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void toTag(CompoundTag inputTag, CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().put("cardinal:components", this.componentContainer.toTag(new CompoundTag()));
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void fromTag(CompoundTag tag, CallbackInfo ci) {
        this.componentContainer.fromTag(tag.getCompound("cardinal:components"));
    }

    @Override
    public ComponentProvider getComponents(@Nullable Direction side) {
        return this.components.getComponents(side);
    }

}
