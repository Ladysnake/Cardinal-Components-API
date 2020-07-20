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
package nerdhub.cardinal.components.api.util.provider;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentProvider;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * A provider that never exposes any component
 */
public final class EmptyComponentProvider implements ComponentProvider {
    private static final ComponentProvider EMPTY_PROVIDER = new EmptyComponentProvider();
    private static final ComponentContainer<?> EMPTY_CONTAINER = StaticComponentPluginBase.createEmptyContainer(Component.class, "EmptyProviderImpl");

    public static ComponentProvider instance() {
        return EMPTY_PROVIDER;
    }

    /**
     * {@inheritDoc}
     * @return {@code false}
     */
    @Override
    public boolean hasComponent(ComponentType<?> type) {
        return false;
    }

    /**
     * {@inheritDoc}
     * @return {@code null}
     */
    @Nullable
    @Override
    public <C extends Component> C getComponent(ComponentType<C> type) {
        return null;
    }

    /**
     * {@inheritDoc}
     * @return an empty set representing this provider's supported component types
     */
    public Set<ComponentType<?>> getComponentTypes() {
        return Collections.emptySet();
    }

    @Nullable
    @Override
    public ComponentContainer<?> getComponentContainer() {
        return EMPTY_CONTAINER;
    }

    private EmptyComponentProvider() {}
}
