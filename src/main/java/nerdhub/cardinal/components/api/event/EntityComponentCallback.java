package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

import java.util.Map;

public interface EntityComponentCallback {

    Event<EntityComponentCallback> EVENT = EventFactory.createArrayBacked(EntityComponentCallback.class, callbacks -> (entity, components) -> {
        for(EntityComponentCallback callback : callbacks) {
            callback.attachComponents(entity, components);
        }
    });

    void attachComponents(Entity entity, Map<ComponentType<? extends Component>, Component> components);
}
