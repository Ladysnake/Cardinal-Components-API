package nerdhub.cardinal.components.util;

import nerdhub.cardinal.components.api.component.ComponentAccessor;
import nerdhub.cardinal.components.api.provider.SidedComponentProvider;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A sided component provider that is always empty.
 */
public final class EmptySidedComponentProvider implements SidedComponentProvider {
    @Override
    public ComponentAccessor getComponents(@Nullable Direction side) {
        return ComponentAccessor.EMPTY;
    }
}

