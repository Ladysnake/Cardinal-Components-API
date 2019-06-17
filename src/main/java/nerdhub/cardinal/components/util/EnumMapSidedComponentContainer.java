package nerdhub.cardinal.components.util;

import java.util.*;

public final class EnumMapSidedComponentContainer implements SidedComponentContainer {
    /* 
     * To limit RAM consumption, delegates are only created when queried for the first time.
     * Because a lot of block entities will only attach components to the core container,
     * the core container instance is used directly in place of an actual independant container.
     * When a side container actually needs to be created (either because someone asked for the
     * actual instance or because a component is to be attached to it), it takes all mappings
     * from the core container and replaces the latter in the corresponding provider.
     */
    private final EnumMap<Direction, ComponentContainer> delegates = new EnumMap<>();
    private final ComponentContainer core = new ArraysComponentContainer();

    private final ComponentAccessor coreProvider = new SimpleComponentProvider(core);
    private final ComponentAccessor northProvider  = new MutableComponentProvider(core);
    private final ComponentAccessor southProvider  = new MutableComponentProvider(core);
    private final ComponentAccessor eastProvider   = new MutableComponentProvider(core);
    private final ComponentAccessor westProvider   = new MutableComponentProvider(core);
    private final ComponentAccessor topProvider    = new MutableComponentProvider(core);
    private final ComponentAccessor bottomProvider = new MutableComponentProvider(core);

    public ComponentContainer get(@Nullable Direction side) {
        if (side == null) {
            return core;
        }
        // If someone wants to interact directly with the side container and there is not one yet, create it
        // and link it to the provider
        return delegates.computeIfAbsent(side, s -> {
            ComponentContainer cc = new ArraysComponentContainer(core);
            ((MutableComponentProvider)getComponentProvider(s)).setBackingContainer(cc);
            return cc;
        });
    }

    /**
     * Gets a component provider for the given side.
     */
    public ComponentAccessor getComponentProvider(@Nullable Direction side) {
        if (side == null) {
            return coreProvider;
        }
        switch (side) {
            case NORTH: return northProvider;
            case SOUTH: return southProvider;
            case EAST: return eastProvider;
            case WEST: return westProvider;
            case TOP: return topProvider;
            case BOTTOM: return bottomProvider;
        }
    }

    default <T> void put(@Nullable Direction side, ComponentType<T> type, T component) {
        if (side == null) {
            // For each side, check that the container has already been created and does not contain the component type
            for (Direction dir : Direction.VALUES) {
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
}