/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2022 OnyxStudios
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

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * @since 2.4.0
 */
public interface EntityComponentFactoryRegistry {

    /**
     * Registers a {@link ComponentFactory} for all instances of a given entity class.
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
    <C extends Component, E extends Entity> void registerFor(Class<E> target, ComponentKey<C> key, ComponentFactory<E, ? extends C> factory);

    /**
     * Registers a {@link ComponentFactory} for all instances of classes that pass the {@code test}.
     *
     * @param test    a predicate testing whether the class can have the component attached to its instances
     * @param key     the key of components to attach
     * @param factory the factory to use to create components of the given key
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    @Deprecated
    <C extends Component> void registerFor(Predicate<Class<? extends Entity>> test, ComponentKey<C> key, ComponentFactory<Entity, C> factory);

    /**
     * Begin a factory registration, initially targeting all instances of the {@code target}.
     *
     * @param target a class object representing the type of entities targeted by the factory
     * @param key    the key of components to attach
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    <C extends Component, E extends Entity> Registration<C, E> beginRegistration(Class<E> target, ComponentKey<C> key);

    /**
     * Registers a {@link ComponentFactory} for all {@link PlayerEntity} instances.
     *
     * @param key     the key of components to attach
     * @param factory the factory to use to create components of the given key
     * @throws NullPointerException if any of the arguments is {@code null}
     * @since 2.6
     */
    <C extends PlayerComponent<? super C>> void registerForPlayers(ComponentKey<? super C> key, ComponentFactory<PlayerEntity, C> factory);

    /**
     * Registers a {@link ComponentFactory} for all {@link PlayerEntity} instances, with a specific {@link RespawnCopyStrategy}.
     *
     * @param key     the key of components to attach
     * @param factory the factory to use to create components of the given key
     * @throws NullPointerException if any of the arguments is {@code null}
     * @see RespawnCopyStrategy#ALWAYS_COPY
     * @see RespawnCopyStrategy#CHARACTER
     * @see RespawnCopyStrategy#INVENTORY
     * @see RespawnCopyStrategy#LOSSLESS_ONLY
     * @see Registration#respawnStrategy(RespawnCopyStrategy)
     * @since 2.5.1
     */
    <C extends Component, P extends C> void registerForPlayers(ComponentKey<C> key, ComponentFactory<PlayerEntity, P> factory, RespawnCopyStrategy<? super P> respawnStrategy);

    interface Registration<C extends Component, E extends Entity> {
        /**
         * Registers a {@link ComponentFactory} for all instances of classes that pass the {@code test}.
         *
         * @param test a predicate testing whether the class can have the component attached to its instances
         */
        Registration<C, E> filter(Predicate<Class<? extends E>> test);

        /**
         * Require that the component factory being registered gets added after the component factory for the given dependency.
         *
         * <p>Component ordering controls order of serialization, synchronization, and ticking, if applicable.
         *
         * <p>An error will be thrown if a circular dependency appears,
         * or if the dependency cannot be satisfied.
         *
         * @param dependency the {@link ComponentKey} describing the component on which to depend
         */
        @ApiStatus.Experimental
        Registration<C, E> after(ComponentKey<?> dependency);

        /**
         * Specify the implementation class that will be produced by the factory.
         *
         * <p>Properties of the component are detected using the available class information.
         * If the implementation is not specified, {@link ComponentKey#getComponentClass()}
         * will be used.
         */
        <I extends C> Registration<I, E> impl(Class<I> impl);

        /**
         * Set the respawn copy strategy used for components of a given type.
         *
         * <p> When a player is cloned as part of the respawn process, its components are copied using
         * a {@link RespawnCopyStrategy}. By default, the strategy used is {@link RespawnCopyStrategy#LOSSLESS_ONLY}.
         * Calling this method allows one to customize the copy process.
         *
         * @param strategy a copy strategy to use when copying components between player instances
         * @see PlayerCopyCallback
         * @see RespawnCopyStrategy#ALWAYS_COPY
         * @see RespawnCopyStrategy#CHARACTER
         * @see RespawnCopyStrategy#INVENTORY
         * @see RespawnCopyStrategy#LOSSLESS_ONLY
         */
        Registration<C, E> respawnStrategy(RespawnCopyStrategy<? super C> strategy);

        /**
         * Complete the ongoing registration.
         *
         * @param factory a factory creating instances of {@code C} that will be attached to instances of {@code E}
         */
        void end(ComponentFactory<E, C> factory);
    }
}
