package nerdhub.cardinal.components.api.util.impl;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.AbstractMap;

public abstract class AbstractComponentContainer extends AbstractMap<ComponentType<?>, Component> implements ComponentContainer {

    @Deprecated
    @Override
    public Component remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        if (key != null && key.getClass() == ComponentType.class) {
            return this.containsKey((ComponentType<?>) key);
        }
        return false;
    }

    @Nullable
    @Override
    public Component get(@Nullable Object key) {
        if (key != null && key.getClass() == ComponentType.class) {
            return get((ComponentType<?>) key);
        }
        return null;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if(tag.containsKey("cardinal_components", NbtType.LIST)) {
            ListTag componentList = tag.getList("cardinal_components", NbtType.COMPOUND);
            componentList.stream().map(CompoundTag.class::cast).forEach(nbt -> {
                ComponentType<?> type = ComponentRegistry.INSTANCE.get(new Identifier(nbt.getString("componentId")));
                Component component = this.get(type);
                if (component != null) {
                    component.fromTag(nbt);
                }
            });
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if(!this.isEmpty()) {
            ListTag componentList = new ListTag();
            this.forEach((type, component) -> {
                CompoundTag componentTag = new CompoundTag();
                componentTag.putString("componentId", type.getId().toString());
                componentList.add(component.toTag(componentTag));
            });
            tag.put("cardinal_components", componentList);
        }
        return tag;
    }
}
