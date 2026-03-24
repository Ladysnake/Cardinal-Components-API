/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2026 Ladysnake
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
package org.ladysnake.cca.internal.leveldata;

import com.google.common.base.Suppliers;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.world.level.storage.LevelData;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v8.component.CardinalComponent;
import org.ladysnake.cca.api.v8.leveldata.LevelDataComponentFactoryRegistry;
import org.ladysnake.cca.api.v8.leveldata.LevelDataComponentInitializer;
import org.ladysnake.cca.internal.base.asm.StaticComponentPluginBase;

import java.util.Collection;
import java.util.function.Supplier;

public final class StaticLevelDataComponentPlugin extends StaticComponentPluginBase<LevelData, LevelDataComponentInitializer> implements LevelDataComponentFactoryRegistry {
    public static final StaticLevelDataComponentPlugin INSTANCE = new StaticLevelDataComponentPlugin();
    public static final Supplier<ComponentContainer.Factory<LevelData>> componentContainerFactory
        = Suppliers.memoize(INSTANCE::buildContainerFactory);

    public static ComponentContainer createContainer(LevelData properties) {
        return componentContainerFactory.get().createContainer(properties);
    }

    private StaticLevelDataComponentPlugin() {
        super("loading a world save", LevelData.class);
    }

    @Override
    protected Collection<EntrypointContainer<LevelDataComponentInitializer>> getEntrypoints() {
        return getComponentEntrypoints("cardinal-components-leveldata", LevelDataComponentInitializer.class);
    }

    @Override
    protected void dispatchRegistration(LevelDataComponentInitializer entrypoint) {
        entrypoint.registerLevelDataComponentFactories(this);
    }

    @Override
    public <C extends CardinalComponent> void register(ComponentKey<C> type, ComponentFactory<LevelData, ? extends C> factory) {
        this.checkLoading(LevelDataComponentFactoryRegistry.class, "register");
        super.register(type, factory);
    }

    @Override
    public <C extends CardinalComponent> void register(ComponentKey<? super C> type, Class<C> impl, ComponentFactory<LevelData, ? extends C> factory) {
        this.checkLoading(LevelDataComponentFactoryRegistry.class, "register");
        super.register(type, impl, factory);
    }
}
