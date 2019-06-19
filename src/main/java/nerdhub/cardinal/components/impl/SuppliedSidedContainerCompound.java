package nerdhub.cardinal.components.impl;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.SidedContainerCompound;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A sided compound component container that uses a supplier to obtain
 * its side components
 */
public final class SuppliedSidedContainerCompound implements SidedContainerCompound {
    private final Map<Direction, ComponentContainer> sides = new EnumMap<>(Direction.class);
    private final Supplier<ComponentContainer> factory;
    private ComponentContainer core;

    public SuppliedSidedContainerCompound(Supplier<ComponentContainer> factory) {
        this.factory = factory;
    }

    @Override
    public ComponentContainer get(@Nullable Direction side) {
        if (side == null) {
            return core == null ? (core = factory.get()) : core;
        }
        return sides.computeIfAbsent(side, d -> factory.get());
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