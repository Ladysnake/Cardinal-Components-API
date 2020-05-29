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
package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Experimental
public final class LazyComponentType<C extends Component> {

    public static LazyComponentType<?> create(Identifier id) {
        return new LazyComponentType<>(id, null);
    }

    public static <C extends Component> LazyComponentType<C> create(Identifier id, Class<C> componentClass) {
        Objects.requireNonNull(componentClass);
        return new LazyComponentType<>(id, componentClass);
    }

    private final Identifier componentId;
    private final Class<C> componentClass;
    private ComponentType<C> wrapped;

    private LazyComponentType(Identifier componentId, @Nullable Class<C> componentClass) {
        this.componentId = componentId;
        this.componentClass = componentClass;
    }

    public boolean isRegistered() {
        return this.tryUnwrap(false) != null;
    }

    // used by generated classes
    public ComponentType<C> unwrap() {
        return this.tryUnwrap(true);
    }

    public void register() {
        if (this.componentClass == null) {
            throw new NullPointerException("Cannot register a ComponentType with an unknown component class");
        }
        ComponentRegistry.INSTANCE.registerIfAbsent(this.componentId, this.componentClass);
    }

    public Identifier getId() {
        return this.componentId;
    }

    /**
     * Convenience method to retrieve a component without explicitly unwrapping a {@link ComponentType}.
     *
     * @param provider a component provider
     * @return the nonnull value of the held component of this type
     * @see ComponentType#get(Object)
     * @see ComponentType#maybeGet(Object)
     */
    public C getComponent(Object provider) {
        return this.unwrap().get(provider);
    }

    @Nullable
    @Contract("true -> !null")
    private ComponentType<C> tryUnwrap(boolean enforceInitialized) {
        ComponentType<C> value = this.wrapped;
        if (value != null) {
            return value;
        }
        ComponentType<?> registered = ComponentRegistry.INSTANCE.get(this.getId());
        if (registered == null && enforceInitialized) {
            if (this.componentClass == null) {
                throw new IllegalStateException("The component type for '" + this.getId()  + "'  was not registered");
            } else {
                ComponentType<C> newValue = ComponentRegistry.INSTANCE.registerIfAbsent(this.getId(), this.componentClass);
                this.wrapped = newValue;
                return newValue;
            }
        } else if (registered != null) {
            if (this.componentClass == null || this.componentClass == registered.getComponentClass()) {
                // either this holder has no type (cast from wildcard to wildcard), or the type has already been checked
                @SuppressWarnings("unchecked") ComponentType<C> newValue = (ComponentType<C>) registered;
                this.wrapped = newValue;
                return newValue;
            } else {
                throw new IllegalStateException("Queried type " + this.componentClass + " mismatches with registered type " + registered.getComponentClass());
            }
        }
        return null;
    }
}
