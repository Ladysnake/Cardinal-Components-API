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

import dev.onyxstudios.cca.api.v3.component.level.LevelComponentFactory;
import dev.onyxstudios.cca.api.v3.component.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.component.level.StaticLevelComponentInitializer;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.util.Identifier;
import net.minecraft.world.WorldProperties;

import java.util.Objects;

public final class StaticLevelComponentPlugin extends StaticComponentPluginBase<WorldProperties, StaticLevelComponentInitializer, LevelComponentFactory<?>> implements LevelComponentFactoryRegistry {
    public static final String LEVEL_IMPL_SUFFIX = "LevelImpl";

    public static final StaticLevelComponentPlugin INSTANCE = new StaticLevelComponentPlugin();

    private StaticLevelComponentPlugin() {
        super("loading a world save", WorldProperties.class, LevelComponentFactory.class, StaticLevelComponentInitializer.class, LEVEL_IMPL_SUFFIX);
    }

    @Override
    protected void dispatchRegistration(StaticLevelComponentInitializer entrypoint) {
        entrypoint.registerLevelComponentFactories(this);
    }

    @Override
    public void register(Identifier componentId, LevelComponentFactory<?> factory) {
        this.checkLoading(LevelComponentFactoryRegistry.class, "register");
        super.register(componentId, (props) -> Objects.requireNonNull(factory.createForSave(props), "Component factory "+ factory + " for " + componentId + " returned null on " + props));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends DynamicContainerFactory<WorldProperties, Component>> getContainerFactoryClass() {
        return (Class<? extends DynamicContainerFactory<WorldProperties, Component>>) super.getContainerFactoryClass();
    }
}
