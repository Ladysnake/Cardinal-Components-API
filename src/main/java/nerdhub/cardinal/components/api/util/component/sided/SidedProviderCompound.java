package nerdhub.cardinal.components.api.util.component.sided;

import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A side-aware component provider.
 */
public interface SidedProviderCompound {

    ComponentProvider getComponents(@Nullable Direction side);

}
