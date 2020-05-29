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
import nerdhub.cardinal.components.internal.ComponentRegistryImpl;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A registry for components.
 *
 * <p> A {@code ComponentRegistry} is used for registering components and obtaining
 * {@link ComponentType} instances serving as keys for those components.
 *
 * @see Component
 * @see ComponentType
 */
@ApiStatus.NonExtendable
public interface ComponentRegistry {
    /**
     * The component registry
     */
    ComponentRegistry INSTANCE = new ComponentRegistryImpl(ComponentType::new);

    /**
     * Registers a component type for the given identifier and class, and returns
     * a shared {@link ComponentType} representation.
     *
     * <p> Calling this method multiple times with the same parameters has the same effect
     * as calling {@link #get(Identifier)} after the first registration call.
     * Calling this method multiple times with the same id but different component classes
     * is forbidden and will throw an {@link IllegalStateException}.
     *
     * @param componentId    a unique identifier for the registered component type
     * @param componentClass the interface or class of which to obtain a {@link ComponentType}
     * @return a shared instance of {@link ComponentType}
     * @throws IllegalArgumentException if {@code componentClass} does not extend {@link Component}
     * @throws IllegalStateException    if a different component class has been registered with the same {@code componentId}
     * @apiNote It is recommended that {@code componentClass} be an interface, so that other
     * mods can interact with a well-defined API rather than directly accessing internals.
     */
    <T extends Component> ComponentType<T> registerIfAbsent(Identifier componentId, Class<T> componentClass);

    /**
     * Directly retrieves a ComponentType using its id.
     *
     * @return the {@code ComponentType} that got registered with {@code id}, or {@code null}
     * if no such {@code ComponentType} is found.
     */
    @Nullable
    ComponentType<?> get(Identifier id);

    /**
     * Lazily retrieve a {@code ComponentType} using its id.
     *
     * <p>If the {@code ComponentType} has not been registered by the time {@link Lazy#get()} is called,
     * an {@code IllegalStateException} is thrown by the latter.
     *
     * @param id the unique identifier of the requested {@code ComponentType}
     * @return a {@code Lazy} describing the {@code ComponentType} that gets registered with {@code id}.
     */
    @ApiStatus.Experimental
    Lazy<ComponentType<?>> getLazy(Identifier id);

    /**
     * Lazily retrieve a {@code ComponentType} using its id.
     *
     * <p>If the {@code ComponentType} has not been registered by the time {@link Lazy#get()} is called,
     * an empty {@code Optional} is returned by the latter.
     *
     * @param id the unique identifier of the requested {@code ComponentType}
     * @return a {@code Lazy} describing the {@code ComponentType} that may get registered with {@code id}.
     * @apiNote this method is especially useful when the component type belongs to an optional dependency
     */
    @ApiStatus.Experimental
    Lazy<Optional<ComponentType<?>>> getLazyOptional(Identifier id);

    /**
     * Return a sequential stream with this registry at its source.
     *
     * @return a sequential {@code Stream} over the component types of this registry.
     */
    Stream<ComponentType<?>> stream();
}
