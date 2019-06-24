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

    /**
     * Example code: 
     * <pre><code>
     * BlockComponentCallback.EVENT.register(b -> b == Blocks.CHEST 
     *      ? (be, scc) -> scc.get(NORTH).put(TYPE, new MyComponent((ChestBlockEntity)be)) 
     *      : null)
     * </code></pre>
     */
    @Nullable
    SidedComponentGatherer<BlockEntity> getComponentGatherer(Block block);
    
}
