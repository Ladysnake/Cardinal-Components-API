package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.component.container.SidedContainerCompound;
import nerdhub.cardinal.components.api.component.provider.ComponentProvider;
import nerdhub.cardinal.components.api.component.provider.SidedProviderCompound;
import nerdhub.cardinal.components.api.util.component.container.IndexedComponentContainer;
import nerdhub.cardinal.components.api.util.component.container.SuppliedSidedContainerCompound;
import nerdhub.cardinal.components.api.util.component.provider.DirectSidedProviderCompound;
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

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fireComponentCallback(CallbackInfo ci) {
        // TODO inject the component gathering into Chunk#setBlockEntity
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
