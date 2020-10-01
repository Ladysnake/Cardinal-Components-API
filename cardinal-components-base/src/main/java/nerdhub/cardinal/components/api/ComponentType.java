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

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import nerdhub.cardinal.components.api.event.ComponentCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

/**
 * The representation of a component type registered through {@link ComponentRegistry#registerIfAbsent}
 *
 * @see ComponentRegistry
 * @see <a href=https://github.com/OnyxStudios/Cardinal-Components-API/blob/master/README.md>information on the V3 API</a>
 * @deprecated use {@link ComponentKey} instead
 */
@Deprecated
@ApiStatus.NonExtendable
public class ComponentType<T extends Component> extends ComponentKey<T> {

    /* ------------ internal methods ------------- */

    /**
     * Constructs a new immutable ComponentType
     *
     * @see ComponentRegistry#registerIfAbsent(Identifier, Class)
     */
    protected ComponentType(Identifier id, Class<T> componentClass, int rawId) {
        super(id, componentClass, rawId);
    }

    @Nullable
    @Override
    public T getInternal(ComponentContainer container) {
        return ((nerdhub.cardinal.components.api.component.ComponentContainer<?>) container).get(this);
    }

    @Nullable
    private T getInternal(ComponentProvider provider) {
        ComponentContainer container = provider.getComponentContainer();

        if (container != null) {
            return this.getInternal(container);
        } else {
            return ((nerdhub.cardinal.components.api.component.ComponentProvider) provider).getComponent(this);
        }
    }

    /* ------------- public methods -------------- */

    /**
     * @deprecated use {@code ObjectPath.fromFunction(}{@link #getNullable(Object) this::getNullable}{@code )}
     * (from the cardinal-components-util module)
     */
    @Deprecated
    public final Function<ComponentProvider, T> asComponentPath() {
        return this::getInternal;
    }

    @Nullable
    @Override
    public <V> T getNullable(V provider) {
        return this.getInternal((ComponentProvider) provider);
    }

    /**
     * @param provider a component provider
     * @param <V>      the class of the component provider
     * @return the nonnull value of the held component of this type
     * @throws NoSuchElementException if the provider does not provide this type of component
     * @throws ClassCastException     if <code>provider</code> does not implement {@link ComponentProvider}
     * @see #maybeGet(Object)
     */
    public final <V> T get(V provider) {
        T component = this.getInternal((ComponentProvider) provider);
        assert component == null || this.getComponentClass().isInstance(component);
        if (component == null) {
            try {
                throw new NoSuchElementException(provider + " provides no component of type " + this.getId());
            } catch (NullPointerException e) {  // some toString implementations crash in init because the name is not set
                NoSuchElementException e1 = new NoSuchElementException(this.getId() + " not available");
                e1.addSuppressed(e);
                throw e1;
            }
        }
        return component;
    }

    /**
     * @param provider a component provider
     * @param <V>      the class of the component provider
     * @return an {@code Optional} describing a component of this type, or an empty
     * {@code Optional} if {@code provider} does not have such a component.
     * @see #get(Object)
     */
    public final <V> Optional<T> maybeGet(@Nullable V provider) {
        if (provider instanceof ComponentProvider) {
            return Optional.ofNullable(this.getInternal((ComponentProvider) provider));
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
     *
     * @param event   the event for which components of this type should be attached
     * @param factory a factory creating instances for this {@code ComponentType}
     * @param <P>     the type of providers targeted by the event
     * @return {@code this}
     * @throws IllegalArgumentException if {@code event} is not a valid component event
     */
    public final <P, C extends T, E extends ComponentCallback<P, ? super C>> ComponentType<T> attach(Event<E> event, Function<P, C> factory) {
        event.register(ComponentsInternals.createCallback(event, this, factory));
        return this;
    }
}
