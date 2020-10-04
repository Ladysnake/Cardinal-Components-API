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
package dev.onyxstudios.cca.api.v3.component;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * A registry for components.
 *
 * <p> A {@code ComponentRegistry} is used for registering components and obtaining
 * {@link ComponentKey} instances serving as keys for those components.
 *
 * @see Component
 * @see ComponentKey
 * @since 2.7.0
 */
@ApiStatus.NonExtendable
public final class ComponentRegistry {
    /**
     * Get a component key for the given identifier and class, or create one if it does not exist.
     *
     * <p>The {@code componentId} must be declared statically, either in a mod's {@code fabric.mod.json} metadata
     * (as a string array custom value element), or through {@link StaticComponentInitializer#getSupportedComponentKeys()}.
     * For example, if {@code componentId}'s value is {@code "foo:bar"}, at least one of the loaded
     * {@code fabric.mod.json} definitions must either declare a {@code "cardinal-components:static-init"} entrypoint,
     * or include the following:
     * <pre><code>
     * "custom": {
     *     "cardinal-components": [
     *         "foo:bar"
     *     ]
     * }
     * </code></pre>.
     *
     * <p> Calling this method multiple times with the same parameters has the same effect
     * as calling {@link #get(Identifier)} after the first registration call.
     * Calling this method multiple times with the same id but different component classes
     * is forbidden and will throw an {@link IllegalStateException}.
     *
     * @param componentId    a unique identifier for the registered component type
     * @param componentClass the interface or class of which to obtain a {@link ComponentKey}
     * @return a shared instance of {@link ComponentKey}
     * @throws IllegalArgumentException if {@code componentClass} does not extend {@link Component}
     * @throws IllegalStateException    if a different component class has been registered with the same {@code componentId},
     *                                  or if {@code componentId} has not been statically declared as a custom data value.
     * @apiNote It is recommended that {@code componentClass} be an interface, so that other
     * mods can interact with a well-defined API rather than directly accessing internals.
     */
    public static <C extends Component> ComponentKey<C> getOrCreate(Identifier componentId, Class<C> componentClass) {
        return ComponentRegistryV3.INSTANCE.getOrCreate(componentId, componentClass);
    }

    /**
     * Directly retrieves a ComponentKey using its id.
     *
     * @return the {@code ComponentKey} that got registered with {@code id}, or {@code null}
     * if no such {@code ComponentKey} is found.
     */
    @Contract(pure = true)
    public static @Nullable ComponentKey<?> get(Identifier id) {
        return ComponentRegistryV3.INSTANCE.get(id);
    }

    /**
     * Return a sequential stream with this registry at its source.
     *
     * @return a sequential {@code Stream} over the component types of this registry.
     */
    @Contract(pure = true)
    public static Stream<ComponentKey<?>> stream() {
        return ComponentRegistryV3.INSTANCE.stream();
    }
}
