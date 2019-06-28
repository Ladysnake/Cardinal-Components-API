package nerdhub.cardinal.components.internal;

import com.google.common.base.Preconditions;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class CardinalEventsInternals {
    private CardinalEventsInternals() { throw new AssertionError(); }

    public static final Event<ItemComponentCallback> WILDCARD_ITEM_EVENT = createItemComponentsEvent();
    private static final Map<Class<? extends Entity>, Event> EVENTS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Event<EntityComponentCallback<T>> event(Class<T> clazz) {
        Preconditions.checkArgument(Entity.class.isAssignableFrom(clazz), "Entity component events must be registered on entity classes.");
        return (Event<EntityComponentCallback<T>>) EVENTS.computeIfAbsent(clazz, c ->
                EventFactory.createArrayBacked(EntityComponentCallback.class, callbacks -> (entity, components) -> {
                for(EntityComponentCallback callback : callbacks) {
                    callback.initComponents(entity, components);
                }
            }));
    }

    public static Event<ItemComponentCallback> createItemComponentsEvent() {
        return EventFactory.createArrayBacked(ItemComponentCallback.class,
            (listeners) -> (stack, components) -> {
                for (ItemComponentCallback listener : listeners) {
                    listener.initComponents(stack, components);
                }
            });
    }
}
