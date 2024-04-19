/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 Ladysnake
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
package org.ladysnake.cca.api.v3.block;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;

/**
 * This class consists exclusively of static methods that return a {@link Component} by querying some block context.
 */
public final class BlockComponents {
    /**
     * Retrieves a context-less {@link BlockApiLookup} for the given {@link ComponentKey}.
     *
     * <p>The component must also be exposed to the lookup by registering a provider
     * for relevant block entities.
     *
     * @param key the key denoting the component for which a block lookup will be retrieved
     * @param <C> the type of the component/API
     * @return a {@link BlockApiLookup} for retrieving instances of {@code C}
     * @see #exposeApi(ComponentKey, BlockApiLookup)
     * @see #exposeApi(ComponentKey, BlockApiLookup, BiFunction)
     * @since 2.8.0
     */
    public static <C extends Component> BlockApiLookup<C, Void> getApiLookup(ComponentKey<C> key) {
        return BlockApiLookup.get(key.getId(), key.getComponentClass(), Void.class);
    }

    /**
     * Exposes an API for all block entities to which a given component is attached.
     *
     * @see #exposeApi(ComponentKey, BlockApiLookup, BiFunction)
     * @since 2.8.0
     */
    public static <A, T> void exposeApi(ComponentKey<? extends A> key, BlockApiLookup<A, T> apiLookup) {
        apiLookup.registerFallback((world, pos, state, blockEntity, context) -> {
            if (blockEntity != null) {
                // yes you can cast <? extends A> to <A>
                @SuppressWarnings("unchecked") A ret = key.getNullable(blockEntity);
                return ret;
            }
            return null;
        });
    }

    /**
     * Exposes an API for all block entities to which a given component is attached.
     *
     * <p><h3>Usage Example</h3>
     * Let us pretend we have the {@code FLUID_CONTAINER} API, as defined in {@link BlockApiLookup}'s usage example.
     *
     * <pre>{@code
     * public interface FluidContainerCompound extends Component {
     *      ComponentKey<FluidContainerCompound> KEY = ComponentRegistry.register(new Identifier("mymod:fluid_container_compound"), FluidContainerCompound.class);
     *
     *      FluidContainer get(Direction side);
     * }
     * }</pre>
     *
     * <pre>{@code
     * @Override
     * public void onInitialize() {
     *     BlockComponents.exposeApi(
     *         FluidContainerCompound.KEY,
     *         MyApi.FLUID_CONTAINER,
     *         FluidContainerCompound::get
     *     );
     * }
     * }</pre>
     *
     * @since 2.8.0
     */
    public static <A, T, C extends Component> void exposeApi(ComponentKey<C> key, BlockApiLookup<A, T> apiLookup, BiFunction<? super C, ? super T, ? extends A> mapper) {
        apiLookup.registerFallback((world, pos, state, blockEntity, context) -> {
            if (blockEntity != null) {
                C ret = key.getNullable(blockEntity);
                if (ret != null) return mapper.apply(ret, context);
            }
            return null;
        });
    }

    /**
     * Exposes an API for block entities of a given type, assuming the given component is attached.
     *
     * <p>This method should be preferred to other overloads as it is more performant than the more generic alternatives.
     * If the component is not {@linkplain BlockComponentFactoryRegistry#registerFor(Class, ComponentKey, ComponentFactory)  attached}
     * to one of the {@code types}, calling {@link BlockApiLookup#find(World, BlockPos, Object)}
     * on the corresponding block will throw a {@link NoSuchElementException}.
     *
     * @see #exposeApi(ComponentKey, BlockApiLookup, BiFunction)
     * @since 2.8.0
     */
    public static <A, T, C extends Component> void exposeApi(ComponentKey<C> key, BlockApiLookup<A, T> apiLookup, BiFunction<? super C, ? super T, ? extends A> mapper, BlockEntityType<?>... types) {
        apiLookup.registerForBlockEntities((blockEntity, context) -> mapper.apply(key.get(blockEntity), context), types);
    }
}
