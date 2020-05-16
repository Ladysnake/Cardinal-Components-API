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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public final class LazyComponentType {
    public static LazyComponentType create(String id) {
        return create(new Identifier(id));
    }

    public static LazyComponentType create(Identifier id) {
        return new LazyComponentType(id);
    }

    private final Identifier componentId;
    private ComponentType<?> value;

    private LazyComponentType(Identifier componentId) {
        this.componentId = componentId;
    }

    public boolean isRegistered() {
        return this.tryGet(false) != null;
    }

    public ComponentType<?> get() {
        return this.tryGet(true);
    }

    public <T extends Component> ComponentType<T> get(Class<T> type) {
        @SuppressWarnings("unchecked") ComponentType<T> ret = (ComponentType<T>) this.get();
        if (!type.isAssignableFrom(ret.getComponentClass())) {
            throw new IllegalStateException("Queried type " + type + " mismatches with registered type " +ret.getComponentClass());
        }
        return ret;
    }

    @Nullable
    @Contract("true -> !null")
    private ComponentType<?> tryGet(boolean enforceInitialized) {
        ComponentType<?> value = this.value;
        if (value == null) {
            ComponentType<?> newValue = ComponentRegistry.INSTANCE.get(this.componentId);
            if (enforceInitialized && newValue == null) {
                throw new IllegalStateException("The component type for '" + this.componentId  +"'  was not registered");
            }
            this.value = newValue;
            return newValue;
        }
        return value;
    }
}
