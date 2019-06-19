package nerdhub.cardinal.components.api.util.impl;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;

import java.util.AbstractMap;

public abstract class AbstractComponentContainer extends AbstractMap<ComponentType<?>, Component> implements ComponentContainer {

    @Override
    public void fromTag(CompoundTag tag) {
        if(tag.containsKey("cardinal_components", NbtType.LIST)) {
            ListTag componentList = tag.getList("cardinal_components", NbtType.COMPOUND);
            componentList.stream().map(CompoundTag.class::cast).forEach(nbt -> {
                ComponentType<?> type = ComponentRegistry.get(new Identifier(nbt.getString("id")));
                Component component = this.get(type);
                if (component != null) {
                    component.deserialize(nbt.getCompound("component"));
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
                componentTag.putString("id", type.getId().toString());
                componentTag.put("component", component.serialize(new CompoundTag()));
                componentList.add(componentTag);
            });
            tag.put("cardinal_components", componentList);
        }
        return tag;
    }
}
