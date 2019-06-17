package nerdhub.cardinal.components.mixins.common;

import com.google.common.collect.ImmutableSet;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.provider.BlockComponentProvider;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(Block.class)
public class MixinBlock implements BlockComponentProvider {

    @Override
    public SidedComponentProvider getComponents(BlockView view, BlockPos pos) {
        // The following optimization assumes that regular blocks never have block entities
        return this.getClass() == Block.class ? SidedComponentProvider.EMPTY : BlockComponentProvider.super.getComponents(view, pos);
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
