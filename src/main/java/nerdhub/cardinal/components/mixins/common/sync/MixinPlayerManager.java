package nerdhub.cardinal.components.mixins.common.sync;

import nerdhub.cardinal.components.api.event.sync.PlayerSyncCallback;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager {
    @Inject(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getStatusEffects()Ljava/util/Collection;"
            )
    )
    private void onPlayerLogIn(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        PlayerSyncCallback.EVENT.invoker().onPlayerSync(player);
    }
}
