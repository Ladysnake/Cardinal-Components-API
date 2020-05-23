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
package nerdhub.cardinal.components.internal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.event.ComponentRegisteredCallback;
import nerdhub.cardinal.components.internal.asm.CcaBootstrap;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public final class ComponentRegistryImpl implements ComponentRegistry {

    // used by generated classes
    public static ComponentType<?> byRawId(@Nonnegative int rawId) {
        ComponentRegistryImpl registry = (ComponentRegistryImpl) INSTANCE;
        ComponentType<?> ret = registry.get(rawId);
        if (ret == null) {
            for (Object2IntMap.Entry<Identifier> entry : registry.id2Raw.object2IntEntrySet()) {
                if (entry.getIntValue() == rawId) {
                    throw new IllegalStateException("The component type for '" + entry.getKey() + "'  was not registered");
                }
            }
            throw new IllegalArgumentException("Invalid raw id " + rawId);
        }
        return ret;
    }

    private final ComponentTypeAccess access;
    private final Object2IntMap<Identifier> id2Raw;
    private ComponentType<?>[] raw2Types;
    private int nextRawId = 0;
    private int size;

    public ComponentRegistryImpl(ComponentTypeAccess access) {
        this.access = access;
        this.id2Raw = new Object2IntOpenHashMap<>(16);
        this.raw2Types = new ComponentType[16];
        this.id2Raw.defaultReturnValue(-1);
    }

    @Override
    public synchronized <T extends Component> ComponentType<T> registerIfAbsent(Identifier componentId, Class<T> componentClass) {
        Preconditions.checkArgument(Component.class.isAssignableFrom(componentClass), "Component interface must extend " + Component.class.getCanonicalName());
        // make sure 2+ components cannot get registered at the same time
        int rawId = this.assignRawId(componentId);
        @SuppressWarnings("unchecked")
        ComponentType<T> existing = (ComponentType<T>) this.get(rawId);
        if (existing != null) {
            if (existing.getComponentClass() != componentClass) {
                throw new IllegalStateException("Registered component " + componentId + " twice with 2 different classes: " + existing.getComponentClass() + ", " + componentClass);
            }
            return existing;
        } else {
            ComponentType<T> registered;
            Class<? extends ComponentType<?>> generated = CcaBootstrap.INSTANCE.getGeneratedComponentTypeClass(componentId);
            if (generated != null) {
                registered = this.instantiateStaticType(generated, componentId, componentClass, rawId);
            } else {
                registered = this.access.create(componentId, componentClass, rawId);
            }
            if (this.raw2Types.length < rawId) {
                this.raw2Types = new ComponentType[this.raw2Types.length + 16];
            }
            this.raw2Types[rawId] = registered;
            this.size++;
            ComponentRegisteredCallback.EVENT.invoker().onComponentRegistered(componentId, componentClass, registered);
            return registered;
        }
    }

    public int size() {
        return this.size;
    }

    public synchronized int assignRawId(Identifier componentId) {
        int existing = this.id2Raw.getInt(componentId);
        if (existing >= 0) {
            return existing;
        }
        int rawId = this.nextRawId;
        this.id2Raw.put(componentId, rawId);
        this.nextRawId++;
        return rawId;
    }

    private <T extends Component> ComponentType<T> instantiateStaticType(Class<? extends ComponentType<?>> generated, Identifier componentId, Class<T> componentClass, int rawId) {
        try {
            @SuppressWarnings("unchecked") ComponentType<T> ret = (ComponentType<T>) generated.getConstructor(Identifier.class, Class.class, int.class).newInstance(componentId, componentClass, rawId);
            return ret;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Failed to create statically declared component type", e);
        }
    }

    @Nullable
    private ComponentType<?> get(int rawId) {
        ComponentType<?>[] raw2Types = this.raw2Types;
        if (rawId >= 0 && rawId < raw2Types.length) {
            return raw2Types[rawId];
        }
        return null;
    }

    @Nullable
    @Override
    public ComponentType<?> get(Identifier id) {
        return this.get(this.id2Raw.getInt(id));
    }

    @Override
    public Lazy<ComponentType<?>> getLazy(Identifier id) {
        int rawId = this.assignRawId(id);
        return new Lazy<>(() -> byRawId(rawId));
    }

    @Override
    public Stream<ComponentType<?>> stream() {
        return Arrays.stream(this.raw2Types).filter(Objects::nonNull);
    }

    @VisibleForTesting
    void clear() {
        this.id2Raw.clear();
        this.raw2Types = new ComponentType[16];
        this.nextRawId = 0;
        this.size = 0;
    }
}
