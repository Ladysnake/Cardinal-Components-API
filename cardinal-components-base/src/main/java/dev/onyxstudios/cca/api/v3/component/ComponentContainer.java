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
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.util.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
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
    static <T> FactoryBuilder<T> factoryBuilder(Class<T> singleArgClass) {
        return new FactoryBuilder<>(singleArgClass);
    }

    @Unmodifiable
    Set<ComponentKey<?>> keys();

    boolean hasComponents();

    default void copyFrom(ComponentContainer other) {
        for (ComponentKey<?> key : this.keys()) {
            Component theirs = key.getInternal(other);
            Component ours = key.getInternal(this);
            assert ours != null;

            if (theirs != null && !ours.equals(theirs)) {
                if (ours instanceof CopyableComponent) {
                    @SuppressWarnings("unchecked") CopyableComponent<Component> copyable = (CopyableComponent<Component>) ours;
                    copyable.copyFrom(theirs);
                } else {
                    ours.fromTag(theirs.toTag(new CompoundTag()));
                }
            }
        }
    }

    /**
     * @param <T> the type of the resulting factory's single argument
     */
    final class FactoryBuilder<T> {
        private static int counter;

        private boolean built;
        private final Class<T> argClass;
        private final Map<Identifier, Function<T, ? extends Component>> factories;

        FactoryBuilder(Class<T> argClass) {
            this.argClass = argClass;
            this.factories = new HashMap<>();
        }

        public <C extends Component> FactoryBuilder<T> component(ComponentKey<C> key, Function<T, ? extends C> factory) {
            this.factories.put(key.getId(), factory);
            return this;
        }

        public Function<T, ComponentContainer> build() {
            if (this.built) throw new IllegalStateException("Cannot build more than one container factory with the same builder");

            try {
                this.built = true;
                String implNameSuffix = "Custom$" + counter++;
                Class<? extends ComponentContainer> containerClass = StaticComponentPluginBase.spinComponentContainer(
                    Function.class, this.factories, implNameSuffix
                );
                Class<? extends Function<T, ComponentContainer>> factoryClass = StaticComponentPluginBase.spinContainerFactory(
                    implNameSuffix, Function.class, containerClass, null, 0, this.argClass
                );
                return ComponentsInternals.createFactory(factoryClass);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
