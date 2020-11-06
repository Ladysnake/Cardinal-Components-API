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
package dev.onyxstudios.cca.internal.scoreboard;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Lazy;

import java.util.Collection;

public final class StaticTeamComponentPlugin extends StaticComponentPluginBase<Team, ScoreboardComponentInitializer> {
    public static final StaticTeamComponentPlugin INSTANCE = new StaticTeamComponentPlugin();
    public static final Lazy<ComponentContainer.Factory<Team>> componentsContainerFactory
        = new Lazy<>(INSTANCE::buildContainerFactory);

    private StaticTeamComponentPlugin() {
        super("made a team", Team.class);
    }

    @Override
    public <C extends Component> void register(ComponentKey<C> key, ComponentFactory<Team, ? extends C> factory) {
        super.register(key, factory);
    }

    @Override
    protected Collection<EntrypointContainer<ScoreboardComponentInitializer>> getEntrypoints() {
        return StaticScoreboardComponentPlugin.INSTANCE.getEntrypoints();
    }

    @Override
    protected void dispatchRegistration(ScoreboardComponentInitializer entrypoint) {
        StaticScoreboardComponentPlugin.INSTANCE.dispatchRegistration(entrypoint);
    }
}
