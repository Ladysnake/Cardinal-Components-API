package nerdhub.cardinal.components.impl;

import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.SidedComponentProvider;
import nerdhub.cardinal.components.api.util.Components;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A sided component provider that is always empty.
 */
public final class EmptySidedComponentProvider implements SidedComponentProvider {
    @Override
    public ComponentProvider getComponents(@Nullable Direction side) {
        return Components.emptyProvider();
    }
}

