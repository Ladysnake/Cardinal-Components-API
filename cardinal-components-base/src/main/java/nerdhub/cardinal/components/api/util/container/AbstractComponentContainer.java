/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 OnyxStudios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package nerdhub.cardinal.components.api.util.container;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
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
import java.util.Set;

/**
 * This class provides a skeletal implementation of the {@code ComponentContainer} interface,
 * to minimize the effort required to implement this interface.
 *
 * <p> To implement an unmodifiable container, the programmer needs only to extend this class and
 * provide an implementation for the {@code entrySet} method, which returns a set-view of the container's
 * mappings. It is however recommended to provide a specific implementation of the {@link ComponentContainer#get(ComponentType)}
 * method, for performance reasons.
 *
 * <p> To implement a modifiable container, the programmer must additionally override the
 * {@link ComponentContainer#put(ComponentType, Component)} method (which otherwise throws an
 * {@code UnsupportedOperationException}).
 *
 * @see AbstractMap
 * @see FastComponentContainer
 */
public abstract class AbstractComponentContainer<C extends Component> extends AbstractMap<ComponentType<?>, C> implements ComponentContainer<C> {

    @SuppressWarnings("unchecked")
    @Override
    public Set<ComponentKey<?>> keys() {
        return (Set<ComponentKey<?>>) (Set<?>) this.keySet();
    }

    @Override
    public Class<C> getComponentClass() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")    // overriding the deprecated method to avoid the compiler's warning...
    @Deprecated
    @Override
    public C remove(Object key) {
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

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public C get(@Nullable Object key) {
        if (key != null && key.getClass() == ComponentType.class) {
            return (C) get((ComponentType<?>) key);
        }
        return null;
    }

    @Override
    public abstract C put(ComponentType<?> key, C value);
    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation first checks if {@code tag} has a tag list
     * mapped to the "cardinal_components" key; if not it returns immediately.
     * Then it iterates over the list's tags, casts them to {@code CompoundTag},
     * and passes them to the associated component's {@code fromTag} method.
     * If this container lacks a corresponding component for a serialized component
     * type, the component tag is skipped.
     */
    @Override
    public void fromTag(CompoundTag tag) {
        if(tag.contains("cardinal_components", NbtType.LIST)) {
            ListTag componentList = tag.getList("cardinal_components", NbtType.COMPOUND);
            for (int i = 0; i < componentList.size(); i++) {
                CompoundTag nbt = componentList.getCompound(i);
                ComponentType<?> type = ComponentRegistry.INSTANCE.get(new Identifier(nbt.getString("componentId")));
                if (type != null) {
                    Component component = this.get(type);
                    if (component != null) {
                        component.fromTag(nbt);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation first checks if the container is empty; if so it
     * returns immediately. Then, it iterates over this container's mappings, and creates
     * a compound tag for each component. The component type's identifier is stored
     * in that tag using the "componentId" key. The tag is then passed to the component's
     * {@link Component#toTag(CompoundTag)} method. Every such serialized component is appended
     * to a {@code ListTag}, that is added to the given tag using the "cardinal_components" key.
     */
    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if(!this.isEmpty()) {
            ListTag componentList = new ListTag();
            for (ComponentKey<?> type : this.keySet()) {
                C component = type.getFromContainer(this);
                CompoundTag componentTag = new CompoundTag();
                componentTag.putString("componentId", type.getId().toString());
                componentList.add(component.toTag(componentTag));
            }
            tag.put("cardinal_components", componentList);
        }
        return tag;
    }
}
