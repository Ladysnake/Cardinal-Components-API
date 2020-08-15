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
package dev.onyxstudios.cca.api.v3.entity;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * @since 2.4.0
 */
@ApiStatus.Experimental
public interface EntityComponentFactoryRegistry {

    /**
     * Registers an {@link EntityComponentFactory} for all instances of a given entity class.
     *
     * <p> Callers of this method should always use the most specific entity
     * type for their use. For example, a factory which goal is to attach a component
     * to players should pass {@code PlayerEntity.class} as a parameter,
     * not one of its superclasses. This limits the need for entity-dependant
     * checks, as well as the amount of redundant callback invocations.
     * For these same reasons, when registering factories for various entity types,
     * it is often better to register a separate specialized callback for each type
     * than a single generic callback with additional checks.
     *
     * @param target  a class object representing the type of entities targeted by the factory
     * @param factory the factory to use to create components of the given type
     */
    <C extends Component, E extends Entity> void registerFor(Class<E> target, ComponentKey<C> type, EntityComponentFactory<C, E> factory);

    /**
     * Registers an {@link EntityComponentFactory} for all instances of classes that pass the {@code test}.
     *
     * @param test  a predicate testing whether the class can have the component attached to its instances
     * @param type the type of components to attach
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    <C extends Component> void registerFor(Predicate<Class<? extends Entity>> test, ComponentKey<C> type, EntityComponentFactory<C, Entity> factory);

}
