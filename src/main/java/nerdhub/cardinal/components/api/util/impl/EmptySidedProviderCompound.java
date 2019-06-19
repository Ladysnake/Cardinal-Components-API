package nerdhub.cardinal.components.api.util.impl;

import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.SidedProviderCompound;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A sided component provider that is always empty.
 */
public final class EmptySidedProviderCompound implements SidedProviderCompound {
    private EmptySidedProviderCompound() { }

    @Override
    public ComponentProvider getComponents(@Nullable Direction side) {
        return EmptyComponentProvider.instance();
    }


    private static final SidedProviderCompound EMPTY_SIDED_PROVIDER = new EmptySidedProviderCompound();
    public static SidedProviderCompound instance() {
        return EMPTY_SIDED_PROVIDER;
    }

}

