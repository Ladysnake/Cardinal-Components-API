package nerdhub.cardinal.components.api.event.sync;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Called when a player joins the server or changes dimension, can be used to synchronize the player's own data
 */
@FunctionalInterface
public interface PlayerSyncCallback {
    Event<PlayerSyncCallback> EVENT = EventFactory.createArrayBacked(PlayerSyncCallback.class, (p) -> {}, listeners -> (player) -> {
        for (PlayerSyncCallback callback : listeners) {
            callback.onPlayerSync(player);
        }
    });

    void onPlayerSync(ServerPlayerEntity player);
}
