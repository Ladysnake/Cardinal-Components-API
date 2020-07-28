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

import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.util.NbtSerializable;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A container for components.
 *
 * <p> Component values obey 2 constraints.
 * <ul>
 *     <li>Every component in a {@code ComponentContainer<C>} is an instance of {@code C}</li>
 *     <li>A component mapped to a {@code ComponentType<T>} is also an instance of {@code T}</li>
 * </ul>
 * Both type constraints should generally be interfaces, to allow multiple inheritance.<br><br>
 *
 * <p> A {@code ComponentContainer} cannot have its components removed.
 * Components can be added or replaced, but removal of existing component types
 * is unsupported. This guarantees consistent behaviour for consumers.
 *
 * @param <C> The upper bound for components stored in this container
 */
@ApiStatus.NonExtendable
@ApiStatus.Experimental
public interface ComponentContainer<C extends Component> extends NbtSerializable {

    /**
     * Create a builder to configure a {@link ComponentContainer} factory.
     *
     * <p>Example code:
     * <pre>{@code
     * private static final Function<ItemStack, ComponentContainer<Component>> CONTAINER_FACTORY =
     *      ComponentContainer.factoryBuilder(ItemStack.class)
     *          .component(MY_COMPONENT_KEY, MyComponent::new)
     *          .component(OTHER_COMPONENT_KEY, stack -> new OtherComponent())
     *          .build();
     *
     * public static ComponentContainer<Component> createContainer(ItemStack stack) {
     *      return CONTAINER_FACTORY.apply(stack);
     * }
     * }</pre>
     *
     * @param singleArgClass the class object representing the single argument the resulting factory will accept
     * @param <T>            the type of the resulting factory's single argument
     * @return a new container factory builder
     */
    @Contract(value = "_ -> new", pure = true)
    static <T> FactoryBuilder<T, Component> factoryBuilder(Class<T> singleArgClass) {
        return factoryBuilder(singleArgClass, Component.class);
    }

    @Contract(value = "_,_ -> new", pure = true)
    static <T, C extends Component> FactoryBuilder<T, C> factoryBuilder(Class<T> singleArgClass, Class<C> storedComponentClass) {
        return new FactoryBuilder<>(singleArgClass, storedComponentClass);
    }

    Set<ComponentKey<?>> keys();

    Class<C> getComponentClass();

    /**
     * @param <T> the type of the resulting factory's single argument
     * @param <C> the common supertype of all components in the container.
     */
    final class FactoryBuilder<T, C extends Component> {
        private static int counter;

        private boolean built;
        private final Class<T> argClass;
        private final Class<? super C> storedComponentType;
        private final Map<Identifier, Function<T, ? extends C>> factories;

        public FactoryBuilder(Class<T> argClass, Class<C> storedComponentType) {
            this.argClass = argClass;
            this.storedComponentType = storedComponentType;
            this.factories = new HashMap<>();
        }

        public <D extends C> FactoryBuilder<T, C> component(ComponentKey<? super D> key, Function<T, D> factory) {
            this.factories.put(key.getId(), factory);
            return this;
        }

        public Function<T, ComponentContainer<C>> build() {
            if (this.built) throw new IllegalStateException("Cannot build more than one container factory with the same builder");
            try {
                this.built = true;
                String implNameSuffix = "Custom$" + counter++;
                Class<? extends ComponentContainer<C>> containerClass = StaticComponentPluginBase.spinComponentContainer(
                    Function.class, this.storedComponentType, this.factories, implNameSuffix
                );
                Class<? extends Function<T, ComponentContainer<C>>> factoryClass = StaticComponentPluginBase.spinContainerFactory(
                    implNameSuffix, Function.class, containerClass, null, 0, this.argClass
                );
                return ComponentsInternals.createFactory(factoryClass);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
