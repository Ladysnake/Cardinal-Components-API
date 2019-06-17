package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public interface EntityComponentCallback<T extends Entity> {

    Map<Class<? extends Entity>, Event> EVENTS = new HashMap<>();

    @SuppressWarnings("unchecked")
    static <T extends Entity> Event<EntityComponentCallback<T>> event(Class<T> clazz) {
        return (Event<EntityComponentCallback<T>>) EVENTS.computeIfAbsent(clazz, c ->
                EventFactory.createArrayBacked(EntityComponentCallback.class, callbacks -> (entity, components) -> {
                for(EntityComponentCallback callback : callbacks) {
                    callback.attachComponents(entity, components);
                }
            })
        );
    }

    void attachComponents(T entity, ComponentContainer components);
}
