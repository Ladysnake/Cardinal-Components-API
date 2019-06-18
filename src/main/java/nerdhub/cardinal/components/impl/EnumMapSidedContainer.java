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
 * A sided compound component container that creates container instances as they are needed
 */
public final class EnumMapSidedContainer implements SidedComponentContainer {
    private final Map<Direction, ComponentContainer> sides = new EnumMap<>(Direction.class);
    private final Supplier<ComponentContainer> factory;
    private ComponentContainer core;

    public LazySidedContainer(Supplier<ComponentContainer> factory) {
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