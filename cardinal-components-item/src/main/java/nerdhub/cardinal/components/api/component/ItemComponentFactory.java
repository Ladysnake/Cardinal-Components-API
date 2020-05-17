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
package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Applied to a method to declare it as a component factory for {@linkplain ItemStack item stacks}.
 *
 * <p>The annotated method must take either no arguments, or 1 argument of type {@link ItemStack}.
 * The return type must be either {@link Component} or a subclass.
 *
 * <p>When invoked, the factory can return either a {@link Component} of the right type, or {@code null}.
 * If the factory method returns {@code null}, the stack will not support that type of component
 * (cf. {@link ComponentProvider#hasComponent(ComponentType)}).
 * @since 2.4.0
 */
@ApiStatus.Experimental
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemComponentFactory {
    /**
     * A magic string replacing an item id, representing every item
     */
    String WILDCARD = "cardinal-components-item:wildcard";

    /**
     * The id of the {@link ComponentType} which this factory makes components for.
     *
     * <p> The returned string must be a valid {@link Identifier}.
     * A {@link ComponentType} with the same id must be registered during mod initialization
     * using {@link ComponentRegistry#registerIfAbsent(Identifier, Class)}.
     *
     * @return a string representing the id of a component type
     */
    String value();

    /**
     * Defines the target item id. The factory will be called for every
     * item stack which id is the same as one of the given strings.
     *
     * <p> The returned array must not be empty, and all its elements must represent
     * either valid {@link Identifier}s, or the {@link #WILDCARD} string.
     * A missing {@code Item} at runtime is not an error, and will simply result in the factory never being called.
     *
     * <p>The magic string {@link #WILDCARD} may be returned instead of
     * a valid item id. A factory registered for the wildcard is called for every item stack ever
     * created. For performance reasons, wildcard callbacks should be avoided
     * where possible.
     *
     * @return one or more item ids, or the {@link #WILDCARD}.
     * @see ItemComponentCallback
     */
    String[] targets() default { WILDCARD };
}
