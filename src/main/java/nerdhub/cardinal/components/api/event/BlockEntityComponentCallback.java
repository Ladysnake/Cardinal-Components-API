
package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.SidedContainerCompound;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

public interface BlockEntityComponentCallback<T extends BlockEntity> {

    Map<Class<? extends BlockEntity>, Event> EVENTS = new HashMap<>();

    @SuppressWarnings("unchecked")
    static <T extends BlockEntity> Event<BlockEntityComponentCallback<T>> event(Class<T> clazz) {
        return (Event<BlockEntityComponentCallback<T>>) EVENTS.computeIfAbsent(clazz, c ->
            EventFactory.createArrayBacked(BlockEntityComponentCallback.class, callbacks -> (be, components) -> {
                for(BlockEntityComponentCallback callback : callbacks) {
                    callback.attachComponents(be, components);
                }
            })
        );
    }

    void attachComponents(T blockEntity, SidedContainerCompound components);
}
