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
package dev.onyxstudios.cca.internal.level;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactory;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.world.WorldProperties;

import java.util.Collection;
import java.util.Objects;

public final class StaticLevelComponentPlugin extends StaticComponentPluginBase<WorldProperties, LevelComponentInitializer, LevelComponentFactory<?>> implements LevelComponentFactoryRegistry {
    public static final String LEVEL_IMPL_SUFFIX = "LevelImpl";

    public static final StaticLevelComponentPlugin INSTANCE = new StaticLevelComponentPlugin();

    private StaticLevelComponentPlugin() {
        super("loading a world save", WorldProperties.class, LevelComponentFactory.class, LEVEL_IMPL_SUFFIX);
    }

    @Override
    protected Collection<EntrypointContainer<LevelComponentInitializer>> getEntrypoints() {
        return FabricLoader.getInstance().getEntrypointContainers("cardinal-components-level", LevelComponentInitializer.class);
    }

    @Override
    protected void dispatchRegistration(LevelComponentInitializer entrypoint) {
        entrypoint.registerLevelComponentFactories(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends DynamicContainerFactory<WorldProperties, Component>> getContainerFactoryClass() {
        return (Class<? extends DynamicContainerFactory<WorldProperties, Component>>) super.getContainerFactoryClass();
    }

    @Override
    public <C extends Component> void register(ComponentKey<C> type, LevelComponentFactory<C> factory) {
        this.checkLoading(LevelComponentFactoryRegistry.class, "register");
        super.register(type.getId(), (props) -> Objects.requireNonNull(((LevelComponentFactory<?>) factory).createForSave(props), "Component factory "+ factory + " for " + type.getId() + " returned null on " + props));
    }
}
