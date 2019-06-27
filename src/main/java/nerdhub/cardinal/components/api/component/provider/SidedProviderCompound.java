package nerdhub.cardinal.components.api.component.provider;

import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A side-aware component provider.
 */
// TODO move to util
public interface SidedProviderCompound {

    ComponentProvider getComponents(@Nullable Direction side);

}
