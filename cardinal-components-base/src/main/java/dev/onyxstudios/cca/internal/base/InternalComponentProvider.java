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

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;

// TODO V3 remove when old ComponentProvider goes
public interface InternalComponentProvider extends ComponentProvider, nerdhub.cardinal.components.api.component.ComponentProvider {
    @Override
    default boolean hasComponent(ComponentType<?> type) {
        return type.isProvidedBy(this);
    }

    @Nullable
    @Override
    default <C extends Component> C getComponent(ComponentType<C> type) {
        return ((nerdhub.cardinal.components.api.component.ComponentContainer<?>) this.getComponentContainer()).get(type);
    }

    @Override
    default Set<ComponentType<?>> getComponentTypes() {
        return Collections.unmodifiableSet(((nerdhub.cardinal.components.api.component.ComponentContainer<?>) this.getComponentContainer()).keySet());
    }

    @Override
    default void forEachComponent(BiConsumer<ComponentType<?>, Component> op) {
        for (ComponentKey<?> key : this.getComponentContainer().keys()) {
            op.accept((ComponentType<?>) key, key.get(this));
        }
    }

    @Override
    @Nonnull
    ComponentContainer getComponentContainer();
}
