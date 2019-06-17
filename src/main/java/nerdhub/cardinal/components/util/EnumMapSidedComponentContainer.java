package nerdhub.cardinal.components.util;

import java.util.*;

public final class EnumMapSidedComponentContainer implements SidedComponentContainer {
    private final EnumMap<Direction, ComponentContainer> delegates = new EnumMap<>();
    private final ComponentContainer core = new ArraysComponentContainer();

    public EnumMapSidedComponentContainer() {
        for (Direction side : Direction.VALUES) {
            delegates.put(side, new ArraysComponentContainer());
        }
    }

    public ComponentContainer get(@Nullable Direction side) {
        if (side == null) {
            return core;
        }
        return delegates.get(side);
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