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
package nerdhub.cardinal.components.util.provider;

import com.google.common.collect.Sets;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentProvider;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * A ComponentProvider that falls back onto another when it is missing a component.
 */
public class FallBackComponentProvider implements ComponentProvider {
    protected ComponentProvider main;
    protected ComponentProvider fallback;

    public FallBackComponentProvider(ComponentProvider main, ComponentProvider fallback) {
        this.main = main;
        this.fallback = fallback;
    }

    /**
     * if this method returns {@code true}, then {@link #getComponent(ComponentType)} <strong>must not</strong> return {@code null} for the same {@link ComponentType}
     */
    @Override
    public boolean hasComponent(ComponentType<?> type) {
        return main.hasComponent(type) || fallback.hasComponent(type);
    }

    /**
     * A component requester should generally call one of the {@link ComponentType} methods
     * instead of calling this directly.
     *
     * @return an instance of the requested component, or {@code null}
     * @see ComponentType#get(Object)
     * @see ComponentType#maybeGet(Object)
     */
    @Nullable
    @Override
    public <C extends Component> C getComponent(ComponentType<C> type) {
        C c = main.getComponent(type);
        return c != null ? c : fallback.getComponent(type);
    }

    /**
     * Returns an unmodifiable view of the union of the two component providers
     * backing this {@code FallBackComponentProvider}. The returned set contains
     * all component types that are contained in either backing set.
     * Iterating over the returned set iterates first over all the component types of
     * the main provider, then over each component type of the fallback provider,
     * in order, that is not contained in the main one.
     *
     * @return an unmodifiable view of the component types of both backing providers
     */
    @Override
    public Set<ComponentType<?>> getComponentTypes() {
        return Sets.union(main.getComponentTypes(), fallback.getComponentTypes());
    }
}


