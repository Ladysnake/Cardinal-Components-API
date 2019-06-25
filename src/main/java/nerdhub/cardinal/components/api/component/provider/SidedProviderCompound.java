package nerdhub.cardinal.components.api.component.provider;

import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A side-aware component provider.
 */
public interface SidedProviderCompound {

    ComponentProvider getComponents(@Nullable Direction side);

}
