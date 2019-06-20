package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public interface EntityComponentCallback<T extends Entity> {

    static <T extends Entity> Event<EntityComponentCallback<T>> event(Class<T> clazz) {
        return CardinalEventsInternals.event(clazz);
    }

    void attachComponents(T entity, ComponentContainer components);
}
