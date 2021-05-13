package dev.onyxstudios.cca.mixin.entity.common;

import dev.onyxstudios.cca.api.v3.entity.TrackingStartCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public abstract class MixinEntityTrackerEntry {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "startTracking", at = @At("RETURN"))
    private void onStartedTracking(ServerPlayerEntity player, CallbackInfo ci) {
        TrackingStartCallback.EVENT.invoker().onPlayerStartTracking(player, this.entity);
    }
}
