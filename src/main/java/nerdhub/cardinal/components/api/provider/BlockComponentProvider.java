package nerdhub.cardinal.components.api.provider;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

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
        return be != null ? (SidedComponentProvider) be : SidedComponentProvider.EMPTY;
    }

    /**
     * if this method returns {@code true}, then {@link #getComponent(BlockView, BlockPos, ComponentType, Direction)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     *
     * @return whether or not this {@link BlockComponentProvider} can provide the desired component
     */
    @Deprecated
    <T extends Component> boolean hasComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side);

    /**
     * @return an instance of the requested component, or {@code null}
     */
    @Deprecated
    <T extends Component> T getComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side);

    @Deprecated
    default <T extends Component> Optional<T> optionally(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        return Optional.ofNullable(getComponent(blockView, pos, type, side));
    }

    /**
     * @return an <strong>immutable</strong> view of the component types exposed by this {@link BlockComponentProvider}
     */
    @Deprecated
    Set<ComponentType<? extends Component>> getComponentTypes(BlockView blockView, BlockPos pos, @Nullable Direction side);
}
