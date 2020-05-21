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
package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.ChunkComponentFactory;
import nerdhub.cardinal.components.api.component.ChunkComponentFactoryRegistry;
import nerdhub.cardinal.components.api.component.StaticChunkComponentInitializer;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.Chunk;

public final class StaticChunkComponentPlugin extends StaticComponentPluginBase<Chunk, StaticChunkComponentInitializer, ChunkComponentFactory<?>> implements ChunkComponentFactoryRegistry {
    public static final String CHUNK_IMPL_SUFFIX = "ChunkImpl";

    public static final StaticChunkComponentPlugin INSTANCE = new StaticChunkComponentPlugin();

    private StaticChunkComponentPlugin() {
        super("loading a chunk", Chunk.class, ChunkComponentFactory.class, StaticChunkComponentInitializer.class, CHUNK_IMPL_SUFFIX);
    }

    @Override
    protected void dispatchRegistration(StaticChunkComponentInitializer entrypoint) {
        entrypoint.registerChunkComponentFactories(this);
    }

    @Override
    public void register(Identifier componentId, ChunkComponentFactory<?> factory) {
        this.checkLoading(ChunkComponentFactoryRegistry.class, "register");
        super.register(componentId, factory);
    }

    @Override
    public Class<? extends DynamicContainerFactory<Chunk, CopyableComponent<?>>> getContainerFactoryClass() {
        @SuppressWarnings("unchecked") Class<? extends DynamicContainerFactory<Chunk, CopyableComponent<?>>> ret = (Class<? extends DynamicContainerFactory<Chunk, CopyableComponent<?>>>) super.getContainerFactoryClass();
        return ret;
    }
}
