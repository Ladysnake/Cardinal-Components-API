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
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Applied to a method to declare it as a component factory for {@linkplain Entity entities}.
 *
 * <p>The annotated method must take either no arguments, or 1 argument of type {@link Entity}
 * or one of its subclasses (eg. {@code PlayerEntity}). The return type must be either {@link Component}
 * or a subclass.
 *
 * <p>When invoked, the factory can return either a {@link Component} of the right type, or {@code null}.
 * If the factory method returns {@code null}, the entity will not support that type of component
 * (cf. {@link ComponentProvider#hasComponent(ComponentType)}).
 */
@ApiStatus.Experimental
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityComponentFactory {
    /**
     * The id of the {@link ComponentType} which this factory makes components for.
     *
     * <p> The returned string must be a valid {@link net.minecraft.util.Identifier}.
     * A {@link ComponentType} with the same id must be registered during mod initialization
     * using {@link ComponentRegistry#registerIfAbsent(Identifier, Class)}.
     *
     * @return a string representing the id of a component type
     */
    String value();

    /**
     * Defines the target entity class. The factory will be called for every
     * entity that {@linkplain Class#isInstance(Object) is an instance} of the given class.
     *
     * <p> If this property is not <strong>explicitly defined</strong>,
     * the target entity class will be inferred from the factory's first
     * parameter. If the factory takes no argument and the target is not explicitly
     * defined, or the factory's first argument is not {@linkplain Class#isAssignableFrom(Class) assignable from}
     * the explicit target class, the factory is invalid.
     *
     * <p> The given value should always be the most specific entity
     * class for the provider's use. For example, a factory which goal is to attach a component
     * to players should use {@code PlayerEntity.class},
     * not one of its superclasses. This limits the need for entity-dependant
     * checks, as well as the amount of redundant factory invocations.
     *
     * @return The class object representing the desired entity type
     */
    Class<? extends Entity> target() default Entity.class;
}
