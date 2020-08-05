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
package dev.onyxstudios.cca.api.component.v3.event;

import dev.onyxstudios.cca.api.component.v3.block.entity.BlockEntityComponentFactory;
import dev.onyxstudios.cca.internal.block.CardinalBlockInternals;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.ComponentCallback;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.entity.BlockEntity;

import net.fabricmc.fabric.api.event.Event;


/**
 * The callback interface for receiving component initialization events
 * during block entity construction.
 *
 * <p> The element that is interested in attaching components
 * to entities implements this interface, and is
 * registered with a class' event, using {@link Event#register(Object)}.
 * When an entity of an applicable type is constructed, the callback's
 * {@link #initComponents} method is invoked.
 *
 * <p> Block entity component callbacks are registered per BE class, and apply to
 * instances of that class and of every subclass. More formally, if a callback
 * is registered for a class {@code E}, its {@code initComponents} method will
 * be invoked for any block entity {@code e} verifying {@code e instanceof E}.
 *
 * @param <E> the type of block entity targeted by this callback
 */
@FunctionalInterface
public interface BlockEntityComponentCallback<E extends BlockEntity> extends ComponentCallback<E, Component> {

    /**
     * Returns the {@code Event} used to register component callbacks for
     * entities of the given type.
     *
     * <p> Callers of this method should always use the most specific entity
     * type for their use. For example, a callback which goal is to attach a component
     * to players should pass {@code PlayerEntity.class} as a parameter,
     * not one of its superclasses. This limits the need for entity-dependant
     * checks, as well as the amount of redundant callback invocations.
     * For these reasons, when registering callbacks for various entity types,
     * it is often better to register a separate specialized callback for each type
     * than a single generic callback with additional checks.
     *
     * @param clazz The class object representing the desired entity type
     * @param <E>   The type of entities targeted by the event
     * @return the {@code Event} used to register component callbacks for entities
     * of the given type.
     * @throws IllegalArgumentException if {@code clazz} is not an entity class
     */
    static <E extends BlockEntity> Event<BlockEntityComponentCallback<E>> event(Class<E> clazz) {
        return CardinalBlockInternals.event(clazz);
    }

    @ApiStatus.Experimental
    static <C extends Component, E extends BlockEntity> void register(ComponentType<C> type, Class<E> targetClass, BlockEntityComponentFactory<C, E> factory) {
        event(targetClass).register((entity, components) -> components.put(type, factory.createForEntity(entity)));
    }

    /**
     * Initialize components for the given block entity.
     *
     * <p>Components that are added to the given container will be available
     * on the block entity as soon as all callbacks have been invoked.
     *
     * <p><strong>The {@code blockEntity} may not be fully initialized when this method is called!</strong>
     * Implementations should resort to lazy initialization if they need properties not available in the
     * base {@link BlockEntity} class.
     *
     * @param blockEntity the block entity being constructed
     * @param components the block entity's component container
     * @implNote Because this method is called for each entity creation, implementations
     * should avoid side effects and keep costly computations at a minimum. Lazy initialization
     * should be considered for components that are costly to initialize.
     */
    @Override
    void initComponents(E blockEntity, ComponentContainer<Component> components);
}
