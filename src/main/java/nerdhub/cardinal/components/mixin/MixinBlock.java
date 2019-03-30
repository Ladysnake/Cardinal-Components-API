package nerdhub.cardinal.components.mixin;

import com.google.common.collect.ImmutableSet;
import nerdhub.cardinal.components.api.ComponentProvider;
import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(Block.class)
public class MixinBlock implements ComponentProvider {

    @Override
    public <T> boolean hasComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof ComponentProvider && ((ComponentProvider) be).hasComponent(blockView, pos, type, side);
    }

    @Nullable
    @Override
    public <T> T getComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof ComponentProvider ? ((ComponentProvider) be).getComponent(blockView, pos, type, side) : null;
    }

    @Override
    public Set<ComponentType<?>> getComponentTypes(BlockView blockView, BlockPos pos, @Nullable Direction side) {
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof ComponentProvider ? ((ComponentProvider) be).getComponentTypes(blockView, pos, side) : ImmutableSet.of();
    }
}
