package nerdhub.cardinal.components.api.provider;

import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A side-aware component provider.
 * TODO rename to SidedCompoundProvider
 */
public interface SidedComponentProvider {

    ComponentProvider getComponents(@Nullable Direction side);

}
