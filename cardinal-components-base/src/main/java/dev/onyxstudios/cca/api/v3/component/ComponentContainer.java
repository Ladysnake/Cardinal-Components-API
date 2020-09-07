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

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import dev.onyxstudios.cca.internal.base.asm.CcaAsmHelper;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.util.NbtSerializable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * An opaque container for components.
 *
 * <p>{@code ComponentContainer}s are <strong>unmodifiable</strong>. After initialization, no component
 * can be added, replaced, or deleted. Component instances themselves can be mutated by third parties.
 */
@ApiStatus.NonExtendable
@ApiStatus.Experimental
public interface ComponentContainer extends NbtSerializable {

    @Contract(pure = true)
    @Unmodifiable Set<ComponentKey<?>> keys();

    @Contract(pure = true)
    boolean hasComponents();

    @Contract(mutates = "this")
    void copyFrom(ComponentContainer other);

    void tickComponents();

    @CheckEnv(Env.CLIENT)
    void tickClientComponents();

    /**
     * A factory for {@link ComponentContainer}s.
     *
     * <p>Instances should be configured through a {@link Builder}.
     *
     * @param <T> the type of the input to the factory
     * @see Factory#builder(Class)
     */
    @ApiStatus.NonExtendable
    interface Factory<T> {
        /**
         * Create a builder to configure a {@link ComponentContainer} factory.
         *
         * <p>Example code:
         * <pre>{@code
         * private static final ComponentContainer.Factory<ItemStack> CONTAINER_FACTORY =
         *      ComponentContainer.Factory.builder(ItemStack.class)
         *          .component(MY_COMPONENT_KEY, MyComponent::new)
         *          .component(OTHER_COMPONENT_KEY, stack -> new OtherComponent())
         *          .build();
         *
         * public static ComponentContainer createContainer(ItemStack stack) {
         *      return CONTAINER_FACTORY.create(stack);
         * }
         * }</pre>
         *
         * @param singleArgClass the class object representing the single argument the resulting factory will accept
         * @param <T>            the type of the resulting factory's input
         * @return a new container factory builder
         */
        @Contract(value = "_ -> new", pure = true)
        static <T> Builder<T> builder(Class<T> singleArgClass) {
            return new Builder<>(singleArgClass);
        }

        /**
         * Instantiates a new {@link ComponentContainer} and populates it with components.
         *
         * <p>The parameter {@code t} will be passed to every factory registered through {@link Builder#component(ComponentKey, Function)}.
         *
         * @param t the factory argument
         * @return a new {@link ComponentContainer}
         * @throws NullPointerException if any of the factories requires a non-null argument
         */
        @Contract("_ -> new")
        ComponentContainer createContainer(@Nullable T t);


        /**
         * @param <T> the type of the resulting factory's single argument
         * @see Factory#builder(Class)
         */
        final class Builder<T> {
            private static int counter;

            private boolean built;
            private final Class<T> argClass;
            private final Map<ComponentKey<?>, Function<T, ? extends Component>> factories;

            Builder(Class<T> argClass) {
                this.argClass = argClass;
                this.factories = new LinkedHashMap<>();
            }

            @Contract(mutates = "this")
            public <C extends Component> Builder<T> component(ComponentKey<C> key, Function<T, ? extends C> factory) {
                this.factories.put(key, factory);
                return this;
            }

            public Factory<T> build() {
                if (this.built) {
                    throw new IllegalStateException("Cannot build more than one container factory with the same builder");
                }

                try {
                    this.built = true;
                    String implNameSuffix = "Custom$" + counter++;
                    Class<? extends ComponentContainer> containerClass = CcaAsmHelper.spinComponentContainer(
                        Function.class, this.factories, implNameSuffix
                    );
                    Class<? extends Factory<T>> factoryClass = StaticComponentPluginBase.spinContainerFactory(
                        implNameSuffix, Factory.class, containerClass, null, 0, this.argClass
                    );
                    return ComponentsInternals.createFactory(factoryClass);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }
}
