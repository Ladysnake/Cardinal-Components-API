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
package org.ladysnake.cca.internal.world;

import com.google.common.base.Suppliers;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;
import org.ladysnake.cca.internal.base.asm.StaticComponentPluginBase;

import java.util.Collection;
import java.util.function.Supplier;

public final class StaticWorldComponentPlugin extends StaticComponentPluginBase<World, WorldComponentInitializer> implements WorldComponentFactoryRegistry {
    public static final StaticWorldComponentPlugin INSTANCE = new StaticWorldComponentPlugin();
    public static final Supplier<ComponentContainer.Factory<World>> componentContainerFactory
        = Suppliers.memoize(INSTANCE::buildContainerFactory);

    private StaticWorldComponentPlugin() {
        super("loading a world", World.class);
    }

    @Override
    protected Collection<EntrypointContainer<WorldComponentInitializer>> getEntrypoints() {
        return getComponentEntrypoints("cardinal-components-world", WorldComponentInitializer.class);
    }

    @Override
    protected void dispatchRegistration(WorldComponentInitializer entrypoint) {
        entrypoint.registerWorldComponentFactories(this);
    }

    @Override
    public <C extends Component> void register(ComponentKey<C> key, ComponentFactory<World, ? extends C> factory) {
        this.checkLoading(WorldComponentFactoryRegistry.class, "register");
        super.register(key, factory);
    }

    @Override
    public <C extends Component> void register(ComponentKey<? super C> key, Class<C> impl, ComponentFactory<World, ? extends C> factory) {
        this.checkLoading(WorldComponentFactoryRegistry.class, "register");
        super.register(key, impl, factory);
    }
}
