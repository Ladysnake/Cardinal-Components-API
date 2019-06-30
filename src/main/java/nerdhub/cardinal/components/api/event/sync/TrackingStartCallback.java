package nerdhub.cardinal.components.api.event.sync;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface TrackingStartCallback {
    Event<TrackingStartCallback> EVENT = EventFactory.createArrayBacked(TrackingStartCallback.class, (p, e) -> {}, listeners -> (player, entity) -> {
        for (TrackingStartCallback callback : listeners) {
            callback.onPlayerStartTracking(player, entity);
        }
    });

    void onPlayerStartTracking(ServerPlayerEntity player, Entity entity);
}
