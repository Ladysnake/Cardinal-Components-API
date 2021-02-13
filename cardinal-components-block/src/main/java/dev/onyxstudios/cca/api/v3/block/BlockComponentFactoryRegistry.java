/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.cca.api.v3.block;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * @since 2.5.0
 */
@ApiStatus.Experimental
public interface BlockComponentFactoryRegistry {

    /**
     * Registers a {@link BlockComponentProvider} for a specific block.
     *
     * @param blockId  the id of a block to target
     * @param factory the factory to use to create components of the given type
     * @throws NullPointerException if any of the arguments is {@code null}
     * @see BlockComponent
     * @see BlockComponents
     */
    <C extends Component> void registerForBlock(Identifier blockId, ComponentKey<? super C> key, BlockComponentProvider<C> factory);

    /**
     * Registers a {@link ComponentFactory} for all instances of a given {@link BlockEntity} class.
     *
     * <p>Callers of this method should always use the most specific block entity
     * type for their use. For example, a factory which goal is to attach a component
     * to furnace-like blocks should pass {@code AbstractFurnaceBlockEntity.class} as a parameter,
     * not one of its superclasses. This limits the need for entity-dependant
     * checks, as well as the amount of redundant callback invocations.
     * For these same reasons, when registering factories for various block entity types,
     * it is often better to register a separate specialized callback for each type
     * than a single generic callback with additional checks.
     *  @param target  a class object representing the type of entities targeted by the factory
     * @param factory the factory to use to create components of the given type
     */
    <C extends Component, BE extends BlockEntity> void registerForBlockEntity(Class<BE> target, ComponentKey<C> key, ComponentFactory<BE, C> factory);

    /**
     * Begin a factory registration, initially targeting all instances of the {@code target}.
     *
     * @param target a class object representing the type of entities targeted by the factory
     * @param key    the key of components to attach
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    @ApiStatus.Experimental
    <C extends Component, B extends BlockEntity> Registration<C, B> beginRegistration(Class<B> target, ComponentKey<C> key);

    @ApiStatus.Experimental
    interface Registration<C extends Component, BE extends BlockEntity> {
        /**
         * Registers a {@link BlockEntityComponentFactory} for all instances of classes that pass the {@code test}.
         *
         * @param test a predicate testing whether the class can have the component attached to its instances
         */
        Registration<C, BE> filter(Predicate<Class<? extends BE>> test);

        /**
         * Specify the implementation class that will be produced by the factory.
         *
         * <p>Properties of the component are detected using the available class information.
         * If the implementation is not specified, {@link ComponentKey#getComponentClass()}
         * will be used.
         */
        <I extends C> Registration<I, BE> impl(Class<I> impl);

        /**
         * Complete the ongoing registration.
         *
         * @param factory a factory creating instances of {@code C} that will be attached to instances of {@code BE}
         */
        void end(ComponentFactory<BE, C> factory);
    }
}
