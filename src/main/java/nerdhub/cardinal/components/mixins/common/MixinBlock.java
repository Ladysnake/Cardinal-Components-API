package nerdhub.cardinal.components.mixins.common;

import com.google.common.collect.ImmutableSet;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.provider.BlockComponentProvider;
import nerdhub.cardinal.components.api.provider.SidedComponentProvider;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(Block.class)
public abstract class MixinBlock implements BlockComponentProvider {

    @Shadow public abstract boolean hasBlockEntity();

    @Override
    public SidedComponentProvider getComponents(BlockView view, BlockPos pos) {
        return this.hasBlockEntity() ? BlockComponentProvider.super.getComponents(view, pos) : SidedComponentProvider.EMPTY;
    }

    @Override
    public <T extends Component> boolean hasComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof BlockComponentProvider && ((BlockComponentProvider) be).hasComponent(blockView, pos, type, side);
    }

    @Nullable
    @Override
    public <T extends Component> T getComponent(BlockView blockView, BlockPos pos, ComponentType<T> type, @Nullable Direction side) {
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof BlockComponentProvider ? ((BlockComponentProvider) be).getComponent(blockView, pos, type, side) : null;
    }

    @Override
    public Set<ComponentType<? extends Component>> getComponentTypes(BlockView blockView, BlockPos pos, @Nullable Direction side) {
        BlockEntity be = blockView.getBlockEntity(pos);
        return be instanceof BlockComponentProvider ? ((BlockComponentProvider) be).getComponentTypes(blockView, pos, side) : ImmutableSet.of();
    }
}
