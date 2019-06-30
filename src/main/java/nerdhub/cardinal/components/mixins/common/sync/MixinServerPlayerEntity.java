package nerdhub.cardinal.components.mixins.common.sync;

import nerdhub.cardinal.components.api.event.sync.PlayerSyncCallback;
import nerdhub.cardinal.components.api.event.sync.TrackingStartCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity {
    @Inject(method = "onStartedTracking", at = @At("HEAD"))
    private void onStartedTracking(Entity tracked, CallbackInfo info) {
        TrackingStartCallback.EVENT.invoker().onPlayerStartTracking((ServerPlayerEntity)(Object)this, tracked);
    }

    @Inject(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getStatusEffects()Ljava/util/Collection;"))
    private void onDimensionChange(DimensionType dimension, CallbackInfoReturnable<Entity> cir) {
        PlayerSyncCallback.EVENT.invoker().onPlayerSync((ServerPlayerEntity)(Object) this);
    }
}
