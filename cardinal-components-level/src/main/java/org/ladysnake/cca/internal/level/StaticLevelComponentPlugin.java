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
package org.ladysnake.cca.internal.level;

import com.google.common.base.Suppliers;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.world.WorldProperties;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.level.LevelComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.level.LevelComponentInitializer;
import org.ladysnake.cca.internal.base.asm.StaticComponentPluginBase;

import java.util.Collection;
import java.util.function.Supplier;

public final class StaticLevelComponentPlugin extends StaticComponentPluginBase<WorldProperties, LevelComponentInitializer> implements LevelComponentFactoryRegistry {
    public static final StaticLevelComponentPlugin INSTANCE = new StaticLevelComponentPlugin();
    public static final Supplier<ComponentContainer.Factory<WorldProperties>> componentContainerFactory
        = Suppliers.memoize(INSTANCE::buildContainerFactory);

    public static ComponentContainer createContainer(WorldProperties properties) {
        return componentContainerFactory.get().createContainer(properties);
    }

    private StaticLevelComponentPlugin() {
        super("loading a world save", WorldProperties.class);
    }

    @Override
    protected Collection<EntrypointContainer<LevelComponentInitializer>> getEntrypoints() {
        return getComponentEntrypoints("cardinal-components-level", LevelComponentInitializer.class);
    }

    @Override
    protected void dispatchRegistration(LevelComponentInitializer entrypoint) {
        entrypoint.registerLevelComponentFactories(this);
    }

    @Override
    public <C extends Component> void register(ComponentKey<C> type, ComponentFactory<WorldProperties, ? extends C> factory) {
        this.checkLoading(LevelComponentFactoryRegistry.class, "register");
        super.register(type, factory);
    }

    @Override
    public <C extends Component> void register(ComponentKey<? super C> type, Class<C> impl, ComponentFactory<WorldProperties, ? extends C> factory) {
        this.checkLoading(LevelComponentFactoryRegistry.class, "register");
        super.register(type, impl, factory);
    }
}
