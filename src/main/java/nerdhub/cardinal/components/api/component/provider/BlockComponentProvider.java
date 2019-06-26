package nerdhub.cardinal.components.api.component.provider;

import nerdhub.cardinal.components.api.util.component.provider.EmptySidedProviderCompound;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * A component provider compound
 */
public interface BlockComponentProvider {

    static BlockComponentProvider get(BlockState state) {
        return get(state.getBlock());
    }

    static BlockComponentProvider get(Block block) {
        return (BlockComponentProvider) block;
    }

    /**
     * Returns the sided component provider for the block at the specified location.
     * Subclasses with block entities must fall back to {@code BlockComponentProvider.super.getComponents(view, pos)}.
     * Failures to obtain a meaningful component provider should result in an empty provider being returned.
     */
    default SidedProviderCompound getComponents(BlockView view, BlockPos pos) {
        BlockEntity be = view.getBlockEntity(pos);
        return be != null ? (SidedProviderCompound) be : EmptySidedProviderCompound.instance();
    }

    <T extends Component> T getComponent(BlockState state, BlockView view, BlockPos pos, Direction side, ComponentType<T> type);

}
