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

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;


/**
 * A component factory for {@linkplain Entity entities}.
 *
 * <p>When invoked, the factory must return a {@link Component} of the right type.
 *
 * @since 2.4.0
 */
@FunctionalInterface
public interface EntityComponentFactory<C extends Component, E extends Entity> {
    /**
     * Create a component of type {@code C} for the given entity.
     *
     * <p>The component returned by this method will be available
     * on the entity as soon as all component factories have been invoked.
     *
     * <p><strong>The {@code entity} may not be fully initialized when this method is called!</strong>
     * Implementations should resort to lazy initialization if they need properties not available in the
     * base {@link Entity} class.
     *
     * @param entity     the entity being constructed
     * @implNote Because this method is called for each entity creation, implementations
     * should avoid side effects and keep costly computations at a minimum. Lazy initialization
     * should be considered for components that are costly to initialize.
     */
    @Nonnull
    C createForEntity(E entity);
}
