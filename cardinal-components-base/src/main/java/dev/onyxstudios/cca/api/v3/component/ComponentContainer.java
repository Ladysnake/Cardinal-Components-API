/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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
import dev.onyxstudios.cca.api.v3.component.load.ClientLoadAwareComponent;
import dev.onyxstudios.cca.api.v3.component.load.ClientUnloadAwareComponent;
import dev.onyxstudios.cca.api.v3.component.load.ServerLoadAwareComponent;
import dev.onyxstudios.cca.api.v3.component.load.ServerUnloadAwareComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import dev.onyxstudios.cca.api.v3.util.NbtSerializable;
import dev.onyxstudios.cca.internal.base.GenericContainerBuilder;
import dev.onyxstudios.cca.internal.base.asm.AsmGeneratedCallback;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Set;

/**
 * An opaque container for components.
 *
 * <p>{@code ComponentContainer}s are <strong>unmodifiable</strong>. After initialization, no component
 * can be added, replaced, or deleted. Component instances themselves can be mutated by third parties.
 */
@ApiStatus.NonExtendable
public interface ComponentContainer extends NbtSerializable {
    ComponentContainer EMPTY = StaticComponentPluginBase.createEmptyContainer();

    @Contract(pure = true)
    @Unmodifiable Set<ComponentKey<?>> keys();

    @Contract(pure = true)
    boolean hasComponents();

    @Contract(mutates = "this")
    void copyFrom(ComponentContainer other);

    @AsmGeneratedCallback(ServerTickingComponent.class)
    void tickServerComponents();

    @CheckEnv(Env.CLIENT)
    @AsmGeneratedCallback(ClientTickingComponent.class)
    void tickClientComponents();

    @ApiStatus.Experimental
    @AsmGeneratedCallback(ServerLoadAwareComponent.class)
    void onServerLoad();

    @ApiStatus.Experimental
    @AsmGeneratedCallback(ServerUnloadAwareComponent.class)
    void onServerUnload();

    @ApiStatus.Experimental
    @CheckEnv(Env.CLIENT)
    @AsmGeneratedCallback(ClientLoadAwareComponent.class)
    void onClientLoad();

    @ApiStatus.Experimental
    @CheckEnv(Env.CLIENT)
    @AsmGeneratedCallback(ClientUnloadAwareComponent.class)
    void onClientUnload();

    /**
     * Reads this object's properties from a {@link NbtCompound}.
     *
     * @param tag a {@code NbtCompound} on which this object's serializable data has been written
     * @implNote implementations must not assert that the data written on the tag corresponds to any
     * specific scheme, as saved data is susceptible to external tempering, and may come from an earlier
     * version. They should also store values into {@code tag} using only unique namespaced keys, as other
     * information may be stored in said tag.
     */
    @Contract(mutates = "this")
    void fromTag(NbtCompound tag);

    /**
     * Writes this object's properties to a {@link NbtCompound}.
     *
     * @param tag a {@code NbtCompound} on which to write this component's serializable data
     * @return {@code tag} for easy chaining
     */
    @Contract(mutates = "param")
    NbtCompound toTag(NbtCompound tag);

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
         * private static final ComponentContainer.Factory<@Nullable Void> CONTAINER_FACTORY =
         *      ComponentContainer.Factory.builder()
         *          .component(MY_COMPONENT_KEY, v -> new MyComponent())
         *          .build();
         *
         * public static ComponentContainer createContainer() {
         *      return CONTAINER_FACTORY.create(null);
         * }
         * }</pre>
         *
         * @return a new container factory builder
         */
        @Contract(value = "-> new", pure = true)
        static Builder<@Nullable Void> builder() {
            return new Builder<>(Void.class);
        }

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
         * <p>The parameter {@code t} will be passed to every factory registered through {@link Builder#component(ComponentKey, ComponentFactory)}.
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
        final class Builder<T> extends GenericContainerBuilder<ComponentFactory<T, ?>, Factory<T>> {
            Builder(Class<T> argClass) {
                super(ComponentFactory.class, Factory.class, List.of(argClass), t -> EMPTY);
            }

            @Contract(mutates = "this")
            public <C extends Component> Builder<T> component(ComponentKey<C> key, ComponentFactory<T, ? extends C> factory) {
                return this.component(key, key.getComponentClass(), factory);
            }

            @Contract(mutates = "this")
            public <C extends Component> Builder<T> component(ComponentKey<? super C> key, Class<C> implClass, ComponentFactory<T, ? extends C> factory) {
                return this.component(key, implClass, factory, Set.of());
            }

            @ApiStatus.Experimental
            @Contract(mutates = "this")
            public <C extends Component> Builder<T> component(ComponentKey<? super C> key, Class<C> implClass, ComponentFactory<T, ? extends C> factory, Set<ComponentKey<?>> dependencies) {
                super.component(key, implClass, factory, dependencies);
                return this;
            }

            /**
             * Sets a suffix for the generated factory class' {@link Class#getName() name}.
             *
             * <p>The suffix can only be set at most once per builder. Not setting a specific suffix will
             * result in a an arbitrary unique suffix being chosen.
             *
             * <p>Reusing the same suffix for two or more factory builders will cause an error at class
             * definition time.
             *
             * @param factoryNameSuffix the unique class suffix for the generated factory object
             * @return {@code this} for chaining
             */
            @Override
            public Builder<T> factoryNameSuffix(String factoryNameSuffix) {
                super.factoryNameSuffix(factoryNameSuffix);
                return this;
            }

            public Factory<T> build() {
                return super.build();
            }

            /**
             * @deprecated use {@link #factoryNameSuffix(String)}
             */
            @Deprecated(forRemoval = true)
            public Factory<T> build(@Nullable String factoryNameSuffix) {
                if (factoryNameSuffix != null) this.factoryNameSuffix(factoryNameSuffix);
                return this.build();
            }
        }
    }
}
