package nerdhub.cardinal.components.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;
import java.util.Collection;

public interface ComponentProvider {

    /**
     * if this method returns {@code true}, then {@link ComponentProvider#getComponent(BlockView, BlockPos, ComponentType, Direction)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     *
     * @return whether or not this {@link ComponentProvider} can provide the desired component
     */
    <T> boolean hasComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side);

    /**
     * @return an instance of the requested component, or {@code null}
     */
    @Nullable
    <T> T getComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side);

    /**
     * @return an <strong>immutable</strong> view of the components exposed by this {@link ComponentProvider}
     */
    Collection<ComponentType<?>> getComponentTypes();
}
