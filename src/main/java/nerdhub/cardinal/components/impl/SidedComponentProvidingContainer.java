package nerdhub.cardinal.components.impl;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.SidedComponentContainer;
import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.SidedComponentProvider;
import nerdhub.cardinal.components.api.util.SimpleComponentProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public final class SidedComponentProvidingContainer implements SidedComponentContainer, SidedComponentProvider {
    /* 
     * To limit RAM consumption, delegates are only created when queried for the first time.
     * Because a lot of block entities will only attach components to the core container,
     * the core container instance is used directly in place of an actual independant container.
     * When a side container actually needs to be created (either because someone asked for the
     * actual instance or because a component is to be attached to it), it takes all mappings
     * from the core container and replaces the latter in the corresponding provider.
     */
    private final Map<Direction, ComponentContainer> delegates = new EnumMap<>(Direction.class);
    private final ComponentContainer core = new IndexedComponentContainer();

    private final ComponentProvider coreProvider = new SimpleComponentProvider(core);
    private final ComponentProvider northProvider  = new MutableComponentProvider(core);
    private final ComponentProvider southProvider  = new MutableComponentProvider(core);
    private final ComponentProvider eastProvider   = new MutableComponentProvider(core);
    private final ComponentProvider westProvider   = new MutableComponentProvider(core);
    private final ComponentProvider topProvider    = new MutableComponentProvider(core);
    private final ComponentProvider bottomProvider = new MutableComponentProvider(core);

    public ComponentContainer get(@Nullable Direction side) {
        if (side == null) {
            return core;
        }
        // If someone wants to interact directly with the side container and there is not one yet, create it
        // and link it to the provider
        return delegates.computeIfAbsent(side, s -> {
            ComponentContainer cc = new IndexedComponentContainer(core);
            ((MutableComponentProvider)getComponents(s)).setBackingContainer(cc);
            return cc;
        });
    }

    @Override
    public ComponentProvider getComponents(@Nullable Direction side) {
        if (side == null) {
            return coreProvider;
        }
        switch (side) {
            case NORTH: return northProvider;
            case SOUTH: return southProvider;
            case EAST: return eastProvider;
            case WEST: return westProvider;
            case UP: return topProvider;
            case DOWN: return bottomProvider;
            default: throw new IllegalStateException();
        }
    }

    @Override
    public <T extends Component> void put(@Nullable Direction side, ComponentType<T> type, T component) {
        if (side == null) {
            // For each side, check that the container has already been created and does not contain the component type
            for (Direction dir : Direction.values()) {
                ComponentContainer cc = delegates.get(dir);
                if (cc != null && !cc.containsKey(type)) {
                    cc.put(type, component);
                }
            }
        }
        get(side).put(type, component);
    }

    @Override
    public void fromTag(CompoundTag serialized) {
        // TODO
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        // TODO
        return tag;
    }

    /**
     * used to access an object's components.
     */
    private static class MutableComponentProvider extends SimpleComponentProvider {
        MutableComponentProvider(ComponentContainer backing) {
            super(backing);
        }

        void setBackingContainer(ComponentContainer backing) {
            this.backing = backing;
        }

    }
}