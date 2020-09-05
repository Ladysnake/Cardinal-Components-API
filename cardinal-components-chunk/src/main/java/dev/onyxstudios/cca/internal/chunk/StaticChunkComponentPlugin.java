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
package dev.onyxstudios.cca.internal.chunk;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactory;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.world.chunk.Chunk;

import java.util.Collection;
import java.util.Objects;

public final class StaticChunkComponentPlugin extends StaticComponentPluginBase<Chunk, ChunkComponentInitializer, ChunkComponentFactory<?>> implements ChunkComponentFactoryRegistry {
    public static final String CHUNK_IMPL_SUFFIX = "ChunkImpl";

    public static final StaticChunkComponentPlugin INSTANCE = new StaticChunkComponentPlugin();

    private StaticChunkComponentPlugin() {
        super("loading a chunk", Chunk.class, ChunkComponentFactory.class, CHUNK_IMPL_SUFFIX);
    }

    @Override
    protected Collection<EntrypointContainer<ChunkComponentInitializer>> getEntrypoints() {
        return FabricLoader.getInstance().getEntrypointContainers("cardinal-components-chunk", ChunkComponentInitializer.class);
    }

    @Override
    protected void dispatchRegistration(ChunkComponentInitializer entrypoint) {
        entrypoint.registerChunkComponentFactories(this);
    }

    @Override
    public Class<? extends DynamicContainerFactory<Chunk>> getContainerFactoryClass() {
        return super.getContainerFactoryClass();
    }

    @Override
    public <C extends Component> void register(ComponentKey<C> type, ChunkComponentFactory<? extends C> factory) {
        this.checkLoading(ChunkComponentFactoryRegistry.class, "register");
        super.register(type, (chunk) -> Objects.requireNonNull(((ChunkComponentFactory<?>) factory).createForChunk(chunk), "Component factory "+ factory + " for " + type.getId() + " returned null on " + chunk.getClass().getSimpleName()));
    }

    @Override
    public <C extends Component> Registration<C> beginRegistration(ComponentKey<C> key) {
        return new RegistrationImpl<>(key);
    }

    private final class RegistrationImpl<C extends Component> implements Registration<C> {
        private final ComponentKey<? super C> key;
        private Class<C> componentClass;

        public RegistrationImpl(ComponentKey<C> key) {
            this.componentClass = key.getComponentClass();
            this.key = key;
        }

        @Override
        public <I extends C> RegistrationImpl<I> impl(Class<I> impl) {
            @SuppressWarnings("unchecked") RegistrationImpl<I> ret = (RegistrationImpl<I>) this;
            ret.componentClass = impl;
            return ret;
        }

        @Override
        public void end(ChunkComponentFactory<C> factory) {
            StaticChunkComponentPlugin.this.checkLoading(Registration.class, "end");
            StaticChunkComponentPlugin.this.register(this.key, factory, this.componentClass);
        }
    }
}
