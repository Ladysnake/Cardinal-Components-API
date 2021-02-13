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
package dev.onyxstudios.cca.api.v3.util;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.internal.base.ComponentRegistryImpl;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Experimental
public final class LazyComponentKey<C extends Component> {

    public static LazyComponentKey<?> create(Identifier id) {
        return new LazyComponentKey<>(id, null);
    }

    public static <C extends Component> LazyComponentKey<C> create(Identifier id, Class<C> componentClass) {
        Objects.requireNonNull(componentClass);
        return new LazyComponentKey<>(id, componentClass);
    }

    private final Identifier componentId;
    private final Class<C> componentClass;
    private ComponentKey<C> wrapped;

    private LazyComponentKey(Identifier componentId, @Nullable Class<C> componentClass) {
        this.componentId = componentId;
        this.componentClass = componentClass;
    }

    public boolean isRegistered() {
        return this.tryUnwrap(false) != null;
    }

    // used by generated classes
    public ComponentKey<C> unwrap() {
        return this.tryUnwrap(true);
    }

    public void register() {
        if (this.componentClass == null) {
            throw new NullPointerException("Cannot register a ComponentKey with an unknown component class");
        }
        ComponentRegistry.getOrCreate(this.componentId, this.componentClass);
    }

    public Identifier getId() {
        return this.componentId;
    }

    /**
     * Convenience method to retrieve a component without explicitly unwrapping a {@link ComponentKey}.
     *
     * @param provider a component provider
     * @return the nonnull value of the held component of this type
     * @see ComponentKey#get(Object)
     * @see ComponentKey#maybeGet(Object)
     */
    public C getComponent(Object provider) {
        return this.unwrap().get(provider);
    }

    @Nullable
    @Contract("true -> !null")
    private ComponentKey<C> tryUnwrap(boolean enforceInitialized) {
        ComponentKey<C> value = this.wrapped;
        if (value != null) {
            return value;
        }
        ComponentKey<?> registered = ComponentRegistryImpl.INSTANCE.get(this.getId());
        if (registered == null && enforceInitialized) {
            if (this.componentClass == null) {
                throw new IllegalStateException("The component type for '" + this.getId()  + "'  was not registered");
            } else {
                ComponentKey<C> newValue = ComponentRegistry.getOrCreate(this.getId(), this.componentClass);
                this.wrapped = newValue;
                return newValue;
            }
        } else if (registered != null) {
            if (this.componentClass == null || this.componentClass == registered.getComponentClass()) {
                // either this holder has no type (cast from wildcard to wildcard), or the type has already been checked
                @SuppressWarnings("unchecked") ComponentKey<C> newValue = (ComponentKey<C>) registered;
                this.wrapped = newValue;
                return newValue;
            } else {
                throw new IllegalStateException("Queried type " + this.componentClass + " mismatches with registered type " + registered.getComponentClass());
            }
        }
        return null;
    }
}
