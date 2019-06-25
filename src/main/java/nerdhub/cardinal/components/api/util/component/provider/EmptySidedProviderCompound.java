package nerdhub.cardinal.components.api.util.component.provider;

import nerdhub.cardinal.components.api.component.provider.ComponentProvider;
import nerdhub.cardinal.components.api.component.provider.SidedProviderCompound;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A sided component provider that is always empty.
 */
public final class EmptySidedProviderCompound implements SidedProviderCompound {

    public static SidedProviderCompound instance() {
        return EMPTY_SIDED_PROVIDER;
    }

    /**
     * {@inheritDoc}
     * @return a {@link ComponentProvider} that is always empty
     * @see EmptyComponentProvider
     */
    @Override
    public ComponentProvider getComponents(@Nullable Direction side) {
        return EmptyComponentProvider.instance();
    }

    private static final SidedProviderCompound EMPTY_SIDED_PROVIDER = new EmptySidedProviderCompound();
    private EmptySidedProviderCompound() { }
}

