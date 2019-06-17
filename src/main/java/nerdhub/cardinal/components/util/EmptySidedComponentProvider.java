package nerdhub.cardinal.components.util;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

/**
 * A sided component provider that is always empty.
 */
public final class EmptySidedComponentProvider {

    public ComponentAccessor getComponents(@Nullable Direction side) {
        return ComponentAccessor.EMPTY;
    }
}

