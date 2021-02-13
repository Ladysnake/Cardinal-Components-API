/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.cca.internal.base;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.internal.base.asm.CcaBootstrap;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

public final class ComponentRegistryImpl implements ComponentRegistryV3 {

    public static final ComponentRegistryImpl INSTANCE = new ComponentRegistryImpl();

    private final Map<Identifier, ComponentKey<?>> keys = new HashMap<>();

    @Override
    public synchronized <T extends Component> ComponentKey<T> getOrCreate(Identifier componentId, Class<T> componentClass) {
        Preconditions.checkArgument(Component.class.isAssignableFrom(componentClass), "Component interface must extend " + Component.class.getCanonicalName());
        // make sure 2+ components cannot get registered at the same time
        @SuppressWarnings("unchecked")
        ComponentKey<T> existing = (ComponentKey<T>) this.get(componentId);

        if (existing != null) {
            if (existing.getComponentClass() != componentClass) {
                throw new IllegalStateException("Registered component " + componentId + " twice with 2 different classes: " + existing.getComponentClass() + ", " + componentClass);
            }
            return existing;
        } else {
            Class<? extends ComponentKey<?>> generated = CcaBootstrap.INSTANCE.getGeneratedComponentTypeClass(componentId);

            if (generated == null) {
                throw new IllegalStateException(componentId + " was not registered through mod metadata or plugin");
            }

            ComponentKey<T> registered = this.instantiateStaticType(generated, componentId, componentClass);
            this.keys.put(componentId, registered);
            return registered;
        }
    }

    private <T extends Component> ComponentKey<T> instantiateStaticType(Class<? extends ComponentKey<?>> generated, Identifier componentId, Class<T> componentClass) {
        try {
            @SuppressWarnings("unchecked") ComponentKey<T> ret = (ComponentKey<T>) generated.getConstructor(Identifier.class, Class.class).newInstance(componentId, componentClass);
            return ret;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Failed to create statically declared component type", e);
        }
    }

    @Nullable
    @Override
    public ComponentKey<?> get(Identifier id) {
        return this.keys.get(id);
    }

    @Override
    public Stream<ComponentKey<?>> stream() {
        return new HashSet<>(this.keys.values()).stream();
    }

    @VisibleForTesting
    void clear() {
        this.keys.clear();
    }
}
