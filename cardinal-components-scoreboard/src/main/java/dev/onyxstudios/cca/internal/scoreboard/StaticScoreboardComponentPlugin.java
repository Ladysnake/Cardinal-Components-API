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
import dev.onyxstudios.cca.api.v3.scoreboard.*;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.scoreboard.Scoreboard;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public final class StaticScoreboardComponentPlugin extends StaticComponentPluginBase<Scoreboard, ScoreboardComponentInitializer, ScoreboardComponentFactoryV2<?>> implements ScoreboardComponentFactoryRegistry {
    public static final String SCOREBOARD_IMPL_SUFFIX = "ScoreboardImpl";

    public static final StaticScoreboardComponentPlugin INSTANCE = new StaticScoreboardComponentPlugin();

    private StaticScoreboardComponentPlugin() {
        super("made a scoreboard", Scoreboard.class, ScoreboardComponentFactoryV2.class, SCOREBOARD_IMPL_SUFFIX);
    }

    @Override
    protected Collection<EntrypointContainer<ScoreboardComponentInitializer>> getEntrypoints() {
        return getComponentEntrypoints("cardinal-components-scoreboard", ScoreboardComponentInitializer.class);
    }

    @Override
    protected void dispatchRegistration(ScoreboardComponentInitializer entrypoint) {
        entrypoint.registerScoreboardComponentFactories(this);
    }

    @Override
    protected Class<? extends DynamicContainerFactory<Scoreboard>> spinContainerFactory(Class<? extends ComponentContainer> containerCls) throws IOException {
        return spinContainerFactory(this.implSuffix, ScoreboardComponentContainerFactory.class, containerCls, null, 0, this.providerClass);
    }

    @Override
    public Class<? extends ScoreboardComponentContainerFactory> getContainerFactoryClass() {
        @SuppressWarnings("unchecked") Class<? extends ScoreboardComponentContainerFactory> t = (Class<? extends ScoreboardComponentContainerFactory>) super.getContainerFactoryClass();
        return t;
    }

    @Override
    public <C extends Component> void register(ComponentKey<C> type, ScoreboardComponentFactory<? extends C> factory) {
        this.registerForScoreboards(type, factory);
    }

    @Override
    public <C extends Component> void registerForScoreboards(ComponentKey<? super C> type, Class<C> impl, ScoreboardComponentFactory<? extends C> factory) {
        this.registerForScoreboards(type, impl, ((ScoreboardComponentFactoryV2<? extends C>) factory));
    }

    @Override
    public <C extends Component> void registerForScoreboards(ComponentKey<? super C> type, ScoreboardComponentFactoryV2<? extends C> factory) {
        this.checkLoading(ScoreboardComponentFactoryRegistry.class, "register");
        super.register(type, (scoreboard, server) -> Objects.requireNonNull(((ScoreboardComponentFactory<?>) factory).createForScoreboard(scoreboard, server), "Component factory "+ factory + " for " + type.getId() + " returned null on " + scoreboard.getClass().getSimpleName()));
    }

    @Override
    public <C extends Component> void registerForScoreboards(ComponentKey<? super C> type, Class<C> impl, ScoreboardComponentFactoryV2<? extends C> factory) {
        this.checkLoading(ScoreboardComponentFactoryRegistry.class, "registerForScoreboards");
        super.register(type, impl, (scoreboard, server) -> Objects.requireNonNull(((ScoreboardComponentFactoryV2<?>) factory).createForScoreboard(scoreboard, server), "Component factory "+ factory + " for " + type.getId() + " returned null on " + scoreboard.getClass().getSimpleName()));
    }

    @Override
    public <C extends Component> void register(ComponentKey<C> type, TeamComponentFactory<? extends C> factory) {
        StaticTeamComponentPlugin.INSTANCE.register(type, factory);
    }

    @Override
    public <C extends Component> void registerForTeams(ComponentKey<? super C> type, Class<C> impl, TeamComponentFactory<? extends C> factory) {
        StaticTeamComponentPlugin.INSTANCE.register(type, impl, factory);
    }

    @Override
    public <C extends Component> void registerForTeams(ComponentKey<? super C> type, TeamComponentFactoryV2<? extends C> factory) {
        StaticTeamComponentPlugin.INSTANCE.register(type, factory);
    }

    @Override
    public <C extends Component> void registerForTeams(ComponentKey<? super C> type, Class<C> impl, TeamComponentFactoryV2<? extends C> factory) {
        StaticTeamComponentPlugin.INSTANCE.register(type, impl, factory);
    }
}
