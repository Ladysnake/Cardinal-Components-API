/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
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
package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.util.ObjectPath;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * The representation of a component type registered through {@link ComponentRegistry#registerIfAbsent}
 *
 * @see ComponentRegistry
 */
public final class ComponentType<T extends Component> {

    private final Class<T> componentClass;
    private final Identifier id;
    private final int rawId;

    /* ------------ internal methods ------------- */

    /**
     * Constructs a new immutable ComponentType
     *
     * @see ComponentRegistry#registerIfAbsent(Identifier, Class)
     */
    /* package-private */
    ComponentType(Identifier id, Class<T> componentClass, int rawId) {
        this.componentClass = componentClass;
        this.id = id;
        this.rawId = rawId;
    }
    
    /* ------------- public methods -------------- */

    public Identifier getId() {
        return this.id;
    }

    public int getRawId() {
        return this.rawId;
    }

    public Class<T> getComponentClass() {
        return componentClass;
    }

    public ObjectPath<ComponentProvider, T> asComponentPath() {
        return c -> c.getComponent(this);
    }

    /**
     * @param provider a component provider
     * @param <V>      the class of the component provider
     * @return the nonnull value of the held component of this type
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see #maybeGet(Object)
     */
    public <V> T get(V provider) {
        T component = (((ComponentProvider) provider)).getComponent(this);
        assert component == null || this.getComponentClass().isInstance(component);
        if (component == null) {
            throw new NoSuchElementException(provider + " provides no component of type " + this.id);
        }
        return component;
    }

    /**
     *
     * @param provider a component provider
     * @param <V>      the class of the component provider
     * @return an {@code Optional} describing a component of this type, or an empty
     * {@code Optional} if {@code provider} does not have such a component.
     * @see #get(Object)
     */
    public <V> Optional<T> maybeGet(@Nullable V provider) {
        if (provider instanceof ComponentProvider) {
            return Optional.ofNullable(((ComponentProvider) provider).getComponent(this));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "ComponentType[\"" + id + "\"]";
    }
}
