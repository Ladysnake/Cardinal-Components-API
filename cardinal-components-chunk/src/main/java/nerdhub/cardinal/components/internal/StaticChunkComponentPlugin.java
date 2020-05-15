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
import net.fabricmc.loader.api.FabricLoader;

public final class StaticChunkComponentPlugin extends StaticComponentPluginBase {
    public static final String CHUNK_IMPL_SUFFIX = "ChunkImpl";
    public static final String CHUNK_CLASS = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_2791");

    public static final StaticChunkComponentPlugin INSTANCE = new StaticChunkComponentPlugin();

    private StaticChunkComponentPlugin() {
        super(CHUNK_CLASS, CHUNK_IMPL_SUFFIX, ChunkComponentFactory.class);
    }
}
