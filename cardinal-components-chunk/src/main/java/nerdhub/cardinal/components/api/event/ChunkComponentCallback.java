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
package nerdhub.cardinal.components.api.event;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactory;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.ApiStatus;

/**
 * The callback interface for receiving component initialization events
 * during chunk construction.
 *
 * <p> The element that is interested in attaching components
 * to entities implements this interface, and is registered
 * with the event, using {@link Event#register(Object)}.
 * When a {@code Chunk} object is constructed, the callback's
 * {@link #initComponents} method is invoked.
 * @see <a href=https://github.com/OnyxStudios/Cardinal-Components-API/blob/master/README.md>information on the V3 API</a>
 * @deprecated implement {@link dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer} instead
 */
@Deprecated
@FunctionalInterface
public interface ChunkComponentCallback extends ComponentCallback<Chunk, CopyableComponent<?>> {

    Event<ChunkComponentCallback> EVENT = EventFactory.createArrayBacked(ChunkComponentCallback.class,
            (callbacks) -> (chunk, components) -> {
                for (ChunkComponentCallback callback : callbacks) {
                    callback.initComponents(chunk, components);
                }
            });

    @ApiStatus.Experimental
    static <C extends CopyableComponent<?>> void register(ComponentType<? super C> type, ChunkComponentFactory<C> factory) {
        EVENT.register((chunk, components) -> components.put(type, factory.createForChunk(chunk)));
    }

    /**
     * Initialize components for the given chunk.
     * Components that are added to the given container will be available
     * on the chunk as soon as all callbacks have been invoked.
     *
     * @param chunk      the chunk being constructed
     * @param components the chunk's component container
     * @implNote Because this method is called for each chunk creation, implementations
     * should avoid side effects and keep costly computations at a minimum. Lazy initialization
     * should be considered for components that are costly to initialize.
     */
    @Override
    void initComponents(Chunk chunk, ComponentContainer<CopyableComponent<?>> components);
}
