package nerdhub.cardinal.components.internal.world;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.PersistentState;

public class ComponentPersistentState extends PersistentState {
    private final ComponentContainer components;

    public ComponentPersistentState(String id, ComponentContainer components) {
        super(id);
        this.components = components;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.components.fromTag(tag);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        return this.components.toTag(tag);
    }
}
