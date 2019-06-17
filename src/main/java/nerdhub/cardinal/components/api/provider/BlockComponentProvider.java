package nerdhub.cardinal.components.api.provider;

import nerdhub.cardinal.components.api.util.Components;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

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
    default SidedComponentProvider getComponents(BlockView view, BlockPos pos) {
        BlockEntity be = view.getBlockEntity(pos);
        return be != null ? (SidedComponentProvider) be : Components.EMPTY_SIDED_PROVIDER;
    }

}
