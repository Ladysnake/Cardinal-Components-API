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

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import dev.onyxstudios.cca.api.v3.scoreboard.TeamComponentFactoryV2;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.scoreboard.Team;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public final class StaticTeamComponentPlugin extends StaticComponentPluginBase<Team, ScoreboardComponentInitializer, TeamComponentFactoryV2<?>> {
    public static final String TEAM_IMPL_SUFFIX = "TeamImpl";

    public static final StaticTeamComponentPlugin INSTANCE = new StaticTeamComponentPlugin();

    private StaticTeamComponentPlugin() {
        super("made a team", Team.class, TeamComponentFactoryV2.class, TEAM_IMPL_SUFFIX);
    }

    @Override
    protected Collection<EntrypointContainer<ScoreboardComponentInitializer>> getEntrypoints() {
        return StaticScoreboardComponentPlugin.INSTANCE.getEntrypoints();
    }

    @Override
    protected void dispatchRegistration(ScoreboardComponentInitializer entrypoint) {
        StaticScoreboardComponentPlugin.INSTANCE.dispatchRegistration(entrypoint);
    }

    @Override
    protected Class<? extends TeamComponentContainerFactory> spinContainerFactory(Class<? extends ComponentContainer> containerCls) throws IOException {
        return spinContainerFactory(this.implSuffix, TeamComponentContainerFactory.class, containerCls, null, 0, this.providerClass);
    }

    @Override
    public Class<? extends TeamComponentContainerFactory> getContainerFactoryClass() {
        @SuppressWarnings("unchecked") Class<? extends TeamComponentContainerFactory> t = (Class<? extends TeamComponentContainerFactory>) super.getContainerFactoryClass();
        return t;
    }

    public <C extends Component> void register(ComponentKey<C> type, TeamComponentFactoryV2<? extends C> factory) {
        super.register(type, (team, scoreboard, server) -> Objects.requireNonNull(((TeamComponentFactoryV2<?>) factory).createForTeam(team, scoreboard, server), "Component factory "+ factory + " for " + type.getId() + " returned null on " + team.getClass().getSimpleName()));
    }

    public <C extends Component> void register(ComponentKey<? super C> type, Class<C> impl, TeamComponentFactoryV2<? extends C> factory) {
        super.register(type, impl, (team, scoreboard, server) -> Objects.requireNonNull(((TeamComponentFactoryV2<?>) factory).createForTeam(team, scoreboard, server), "Component factory "+ factory + " for " + type.getId() + " returned null on " + team.getClass().getSimpleName()));
    }
}
