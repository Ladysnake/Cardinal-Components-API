package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.SidedContainerCompound;
import nerdhub.cardinal.components.internal.BlockEntityTypeCaller;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

@FunctionalInterface
public interface BlockEntityComponentCallback {

    Event<BlockEntityComponentCallback> EVENT = EventFactory.createArrayBacked(BlockEntityComponentCallback.class,
            (callbacks) -> (b) -> {
                SidedComponentGatherer<BlockEntity> ret = (be -> ((SelfSidedComponentGatherer) be).initComponents(cc));
                for (BlockEntityComponentCallback callback : callbacks) {
                    SidedComponentGatherer<BlockEntity> g = callback.getComponentGatherer(b);
                    if (g != null) {
                        ret = ret.andThen(g);
                    }
                }
                return ret;
            })

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
