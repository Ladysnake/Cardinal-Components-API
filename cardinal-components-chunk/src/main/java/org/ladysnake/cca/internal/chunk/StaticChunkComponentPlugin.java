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
package org.ladysnake.cca.internal.chunk;

import com.google.common.base.Suppliers;
import org.ladysnake.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.chunk.ChunkComponentInitializer;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.internal.base.asm.StaticComponentPluginBase;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.world.chunk.Chunk;

import java.util.Collection;
import java.util.function.Supplier;

public final class StaticChunkComponentPlugin extends StaticComponentPluginBase<Chunk, ChunkComponentInitializer> implements ChunkComponentFactoryRegistry {
    public static final StaticChunkComponentPlugin INSTANCE = new StaticChunkComponentPlugin();
    private static final Supplier<ComponentContainer.Factory<Chunk>> componentsContainerFactory
        = Suppliers.memoize(INSTANCE::buildContainerFactory);

    public static ComponentContainer createContainer(Chunk chunk) {
        return componentsContainerFactory.get().createContainer(chunk);
    }

    private StaticChunkComponentPlugin() {
        super("loading a chunk", Chunk.class);
    }

    @Override
    protected Collection<EntrypointContainer<ChunkComponentInitializer>> getEntrypoints() {
        return getComponentEntrypoints("cardinal-components-chunk", ChunkComponentInitializer.class);
    }

    @Override
    protected void dispatchRegistration(ChunkComponentInitializer entrypoint) {
        entrypoint.registerChunkComponentFactories(this);
    }

    @Override
    public <C extends Component> void register(ComponentKey<C> type, ComponentFactory<Chunk, ? extends C> factory) {
        this.register(type, type.getComponentClass(), factory);
    }

    @Override
    public <C extends Component> void register(ComponentKey<? super C> type, Class<C> impl, ComponentFactory<Chunk, ? extends C> factory) {
        this.checkLoading(ChunkComponentFactoryRegistry.class, "register");
        super.register(type, factory);
    }
}
