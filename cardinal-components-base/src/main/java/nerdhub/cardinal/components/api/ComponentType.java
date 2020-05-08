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
package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.event.ComponentCallback;
import nerdhub.cardinal.components.api.util.ObjectPath;
import nerdhub.cardinal.components.internal.ComponentsInternals;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

/**
 * The representation of a component type registered through {@link ComponentRegistry#registerIfAbsent}
 *
 * @see ComponentRegistry
 */
@ApiStatus.NonExtendable
public class ComponentType<T extends Component> {

    private final Class<T> componentClass;
    private final Identifier id;
    private final int rawId;

    /* ------------ internal methods ------------- */

    /**
     * Constructs a new immutable ComponentType
     *
     * @see ComponentRegistry#registerIfAbsent(Identifier, Class)
     */
    protected ComponentType(Identifier id, Class<T> componentClass, int rawId) {
        this.componentClass = componentClass;
        this.id = id;
        this.rawId = rawId;
    }

    @SuppressWarnings("WeakerAccess")   // overridden by generated types
    @Nullable
    protected T get0(ComponentProvider provider) {
        return provider.getComponent(this);
    }

    /* ------------- public methods -------------- */

    public Identifier getId() {
        return this.id;
    }

    @ApiStatus.Internal
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
        T component = get0((ComponentProvider) provider);
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
            return Optional.ofNullable(get0((ComponentProvider) provider));
        }
        return Optional.empty();
    }

    /**
     * Attaches components of this type to a component provider using a {@link ComponentCallback} for that
     * type of provider.
     *
     * <p> This method behaves as if:
     * <pre>{@code
     *      event.register((provider, components) -> components.put(type, factory.apply(provider)));
     * }</pre>
     * @param event the event for which components of this type should be attached
     * @param factory a factory creating instances for this {@code ComponentType}
     * @param <P> the type of providers targeted by the event
     * @return {@code this}
     * @throws IllegalArgumentException if {@code event} is not a valid component event
     */
    public <P, C extends T, E extends ComponentCallback<P, ? super C>> ComponentType<T> attach(Event<E> event, Function<P, C> factory) {
        event.register(ComponentsInternals.createCallback(event, this, factory));
        return this;
    }

    @Override
    public String toString() {
        return "ComponentType[\"" + id + "\"]";
    }
}
