
package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

import java.util.Map;

public interface BlockEntityComponentCallback<T extends BlockEntity> {

    Map<Class<? extends BlockEntity>, Event<BlockEntityComponentCallback>> EVENTS = new HashMap<>();

    @SuppressWarnings("unchecked")
    static <T extends BlockEntity> BlockEntityComponentCallback<T> event(Class<T> clazz) {
        return (BlockEntityComponentCallback<T>) EVENTS.computeIfAbsent(clazz, c -> 
            EventFactory.createArrayBacked(BlockEntityComponentCallback.class, callbacks -> (be, components) -> {
                for(BlockEntityComponentCallback callback : callbacks) {
                    callback.attachComponents(be, components);
                }
            })
        );
    }

    void attachComponents(T blockEntity, SidedComponentContainer components);
}
