package nerdhub.cardinal.components.api.util.impl;

import nerdhub.cardinal.components.api.component.SidedContainerCompound;
import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.SidedProviderCompound;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A sided compound provider that holds direct references to every side's provider.
 */
public final class DirectSidedProviderCompound implements SidedProviderCompound {
    private final SidedContainerCompound backing;

    private final ComponentProvider coreProvider;
    private final ComponentProvider northProvider;
    private final ComponentProvider southProvider;
    private final ComponentProvider eastProvider;
    private final ComponentProvider westProvider;
    private final ComponentProvider upProvider;
    private final ComponentProvider downProvider;

    public DirectSidedProviderCompound(SidedContainerCompound backing) {
        this.backing = backing;
        this.coreProvider   = new SimpleComponentProvider(backing.get(null));
        this.northProvider  = createSideProvider(Direction.NORTH);
        this.southProvider  = createSideProvider(Direction.SOUTH);
        this.eastProvider   = createSideProvider(Direction.EAST);
        this.westProvider   = createSideProvider(Direction.WEST);
        this.upProvider = createSideProvider(Direction.UP);
        this.downProvider = createSideProvider(Direction.DOWN);
    }

    private ComponentProvider createSideProvider(Direction side) {
        return new FallBackComponentProvider(new SimpleComponentProvider(backing.get(side)), coreProvider);
    }

    @Override
    public ComponentProvider getComponents(@Nullable Direction side) {
        if (side == null) {
            return coreProvider;
        }
        switch (side) {
            case NORTH: return northProvider;
            case SOUTH: return southProvider;
            case EAST:  return eastProvider;
            case WEST:  return westProvider;
            case UP:    return upProvider;
            case DOWN:  return downProvider;
            default: throw new IllegalStateException("Unrecognized direction: " + side);
        }
    }
}