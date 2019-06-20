
package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.SidedContainerCompound;
import nerdhub.cardinal.components.internal.BlockEntityTypeCaller;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

@FunctionalInterface
public interface BlockEntityComponentCallback<T extends BlockEntity> {

    @SuppressWarnings("unchecked")
    static <T extends BlockEntity> Event<BlockEntityComponentCallback<T>> event(BlockEntityType<T> type) {
        return (Event<BlockEntityComponentCallback<T>>) ((BlockEntityTypeCaller)type).getBlockEntityComponentEvent();
    }

    void attachComponents(T blockEntity, SidedContainerCompound components);
}
