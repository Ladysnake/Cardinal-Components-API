package nerdhub.cardinal.components.api.provider;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentAccessor;
import nerdhub.cardinal.components.util.EmptySidedComponentProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

/**
 * A side-aware component provider.
 */
public interface SidedComponentProvider {
    SidedComponentProvider EMPTY = new EmptySidedComponentProvider();

    ComponentAccessor getComponents(@Nullable Direction side);

    /**
     * if this method returns {@code true}, then {@link #getComponent(BlockView, BlockPos, ComponentType, Direction)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     *
     * @return whether or not this {@link SidedComponentProvider} can provide the desired component
     */
    @Deprecated
    default <T extends Component> boolean hasComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {return false;}

    /**
     * @return an instance of the requested component, or {@code null}
     */
    @Deprecated
    default <T extends Component> T getComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {return null;}

    @Deprecated
    default <T extends Component> Optional<T> optionally(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        return Optional.ofNullable(getComponent(blockView, pos, type, side));
    }

    /**
     * @return an <strong>immutable</strong> view of the component types exposed by this {@link SidedComponentProvider}
     */
    @Deprecated
    default Set<ComponentType<? extends Component>> getComponentTypes(BlockView blockView, BlockPos pos, @Nullable Direction side) {return null;}
}
