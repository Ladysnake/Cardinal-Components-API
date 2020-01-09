package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.ComponentType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

public interface ComponentRegisteredCallback {
    /**
     * Called when a {@link nerdhub.cardinal.components.api.ComponentType} is registered
     */
    Event<ComponentRegisteredCallback> EVENT = EventFactory.createArrayBacked(ComponentRegisteredCallback.class,
        listeners -> (Identifier id, Class<?> componentClass, ComponentType<?> type) -> {
            for (ComponentRegisteredCallback listener : listeners) {
                listener.onComponentRegistered(id, componentClass, type);
            }
        });

    void onComponentRegistered(Identifier id, Class<?> componentClass, ComponentType<?> type);
}
