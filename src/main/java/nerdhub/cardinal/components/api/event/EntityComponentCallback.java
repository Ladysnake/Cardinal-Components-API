package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.container.ComponentContainer;
import nerdhub.cardinal.components.internal.CardinalEventsInternals;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.entity.Entity;

public interface EntityComponentCallback<T extends Entity> {

    static <T extends Entity> Event<EntityComponentCallback<T>> event(Class<T> clazz) {
        return CardinalEventsInternals.event(clazz);
    }

    void attachComponents(T entity, ComponentContainer components);
}
