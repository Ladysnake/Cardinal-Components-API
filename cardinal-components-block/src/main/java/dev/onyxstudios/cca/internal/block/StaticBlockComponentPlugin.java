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
package dev.onyxstudios.cca.internal.block;

import dev.onyxstudios.cca.api.v3.block.BlockComponentFactory;
import dev.onyxstudios.cca.api.v3.block.*;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public final class StaticBlockComponentPlugin extends LazyDispatcher implements BlockComponentFactoryRegistry {
    public static final StaticBlockComponentPlugin INSTANCE = new StaticBlockComponentPlugin();

    private StaticBlockComponentPlugin() {
        super("instantiating a BlockEntity");
    }

    private static String getSuffix(Class<?> entityClass) {
        String simpleName = entityClass.getSimpleName();
        return String.format("BlockEntityImpl_%s_%s", simpleName, Integer.toHexString(entityClass.getName().hashCode()));
    }

    @Override
    public <C extends BlockComponent> void registerFor(Identifier blockId, Direction side, ComponentKey<? super C> key, BlockComponentFactory<C> factory) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public <C extends Component, BE extends BlockEntity> void registerFor(Class<BE> target, Direction side, ComponentKey<C> key, BlockEntityComponentFactory<C, BE> factory) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    protected void init() {
        StaticComponentPluginBase.processInitializers(
            FabricLoader.getInstance().getEntrypointContainers("cardinal-components-block", BlockComponentInitializer.class),
            initializer -> initializer.registerBlockComponentFactories(this)
        );
    }

}
