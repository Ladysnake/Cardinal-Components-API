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
package nerdhub.cardinal.components.internal.world;

import nerdhub.cardinal.components.api.component.StaticWorldComponentInitializer;
import nerdhub.cardinal.components.api.component.WorldComponentFactory;
import nerdhub.cardinal.components.api.component.WorldComponentFactoryRegistry;
import nerdhub.cardinal.components.internal.StaticComponentPluginBase;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public final class StaticWorldComponentPlugin extends StaticComponentPluginBase<World, StaticWorldComponentInitializer, WorldComponentFactory<?>> implements WorldComponentFactoryRegistry {
    public static final String WORLD_IMPL_SUFFIX = "WorldImpl";

    public static final StaticWorldComponentPlugin INSTANCE = new StaticWorldComponentPlugin();

    private StaticWorldComponentPlugin() {
        super("loading a world", World.class, WorldComponentFactory.class, StaticWorldComponentInitializer.class, WORLD_IMPL_SUFFIX);
    }

    @Override
    protected void dispatchRegistration(StaticWorldComponentInitializer entrypoint) {
        entrypoint.registerWorldComponentFactories(this);
    }

    @Override
    public void register(Identifier componentId, WorldComponentFactory<?> factory) {
        this.checkLoading(WorldComponentFactoryRegistry.class, "register");
        super.register(componentId, factory);
    }
}
