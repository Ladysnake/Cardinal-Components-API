package nerdhub.cardinal.components.impl;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.SidedComponentContainer;
import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.SidedComponentProvider;
import nerdhub.cardinal.components.api.util.Components;
import nerdhub.cardinal.components.api.util.SimpleComponentProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/**
 * A sided compound provider that holds direct references to every side's provider.
 */
// TODO rename to DirectSidedCompoundProvider
public final class SidedComponentProvidingContainer implements SidedComponentProvider {
    private final SidedComponentContainer backing;

    private final ComponentProvider coreProvider;
    private final ComponentProvider northProvider;
    private final ComponentProvider southProvider;
    private final ComponentProvider eastProvider;
    private final ComponentProvider westProvider;
    private final ComponentProvider topProvider;
    private final ComponentProvider bottomProvider;

    public SidedComponentProvidingContainer(SidedComponentContainer backing) {
        this.backing = backing;
        this.coreProvider   = new SimpleComponentProvider(backing.get(null));
        this.northProvider  = createSideProvider(Direction.NORTH);
        this.southProvider  = createSideProvider(Direction.SOUTH);
        this.eastProvider   = createSideProvider(Direction.EAST);
        this.westProvider   = createSideProvider(Direction.WEST);
        this.topProvider    = createSideProvider(Direction.TOP);
        this.bottomProvider = createSideProvider(Direction.BOTTOM);
    }

    private ComponentProvider createSideProvider(Direction side) {
        return Components.fallBackProvider(backing.get(side), coreProvider);
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
            case UP:    return topProvider;
            case DOWN:  return bottomProvider;
            default: throw new IllegalStateException("Unrecognized direction: " + side);
        }
    }
}