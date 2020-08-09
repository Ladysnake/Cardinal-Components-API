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

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.level.LevelProperties;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

/**
 * used to access an object's components.
 */
@ApiStatus.Experimental
public interface ComponentProvider {

    /**
     * Convenience method to retrieve ComponentProvider from a given {@link BlockEntity}
     * Requires the <tt>cardinal-components-block</tt> module.
     */
    static ComponentProvider fromBlockEntity(BlockEntity blockEntity) {
        return (ComponentProvider) blockEntity;
    }

    /**
     * Convenience method to retrieve ComponentProvider from a given {@link ItemStack}
     * Requires the <tt>cardinal-components-item</tt> module.
     */
    @SuppressWarnings("ConstantConditions")
    static ComponentProvider fromItemStack(ItemStack stack) {
        return (ComponentProvider) (Object) stack;
    }

    /**
     * Convenience method to retrieve a ComponentProvider from a given {@link Entity}.
     * Requires the <tt>cardinal-components-entity</tt> module.
     */
    static ComponentProvider fromEntity(Entity entity) {
        return (ComponentProvider) entity;
    }

    /**
     * Convenience method to retrieve a ComponentProvider from a given {@link World}.
     * Requires the <tt>cardinal-components-world</tt> module.
     */
    static ComponentProvider fromWorld(World world) {
        return (ComponentProvider) world;
    }

    /**
     * Convenience method to retrieve a ComponentProvider from given {@link LevelProperties}.
     * Requires the <tt>cardinal-components-level</tt> module.
     */
    static ComponentProvider fromLevel(WorldProperties level) {
        return (ComponentProvider) level;
    }

    /**
     * Convenience method to retrieve a ComponentProvider from given {@link Chunk}.
     * Requires the <tt>cardinal-components-chunk</tt> module.
     */
    static ComponentProvider fromChunk(Chunk chunk) {
        return (ComponentProvider) chunk;
    }

    /**
     * @return a runtime-generated component container storing statically declared components,
     * or {@code null} if this container does not support static components.
     */
    @ApiStatus.Experimental
    @Nullable   // TODO mark NonNull when dynamic containers are gone
    ComponentContainer<?> getComponentContainer();
}
