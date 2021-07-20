/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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

import com.google.common.base.Suppliers;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentFactoryV2;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import dev.onyxstudios.cca.api.v3.scoreboard.TeamComponentFactoryV2;
import dev.onyxstudios.cca.internal.base.GenericContainerBuilder;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class StaticScoreboardComponentPlugin extends LazyDispatcher implements ScoreboardComponentFactoryRegistry {
    public static final StaticScoreboardComponentPlugin INSTANCE = new StaticScoreboardComponentPlugin();
    public static final Supplier<ScoreboardComponentContainerFactory> scoreboardComponentsContainerFactory
        = Suppliers.memoize(INSTANCE::buildScoreboardContainerFactory);
    public static final Supplier<TeamComponentContainerFactory> teamComponentsContainerFactory
        = Suppliers.memoize(INSTANCE::buildTeamContainerFactory);

    private final GenericContainerBuilder.SimpleImpl<ScoreboardComponentFactoryV2<?>, ScoreboardComponentContainerFactory> scoreboardFactories = new GenericContainerBuilder.SimpleImpl<>();
    private final GenericContainerBuilder.SimpleImpl<TeamComponentFactoryV2<?>, TeamComponentContainerFactory> teamFactories = new GenericContainerBuilder.SimpleImpl<>();

    private ScoreboardComponentContainerFactory buildScoreboardContainerFactory() {
        this.ensureInitialized();

        return scoreboardFactories.build(
            "ScoreboardImpl",
            (sc, s) -> ComponentContainer.EMPTY,
            ScoreboardComponentFactoryV2.class,
            ScoreboardComponentContainerFactory.class,
            List.of(Scoreboard.class, MinecraftServer.class)
        );
    }

    private TeamComponentContainerFactory buildTeamContainerFactory() {
        this.ensureInitialized();

        return teamFactories.build(
            "TeamImpl",
            (t, sc, s) -> ComponentContainer.EMPTY,
            TeamComponentFactoryV2.class,
            TeamComponentContainerFactory.class,
            List.of(Team.class, Scoreboard.class, MinecraftServer.class)
        );
    }

    private StaticScoreboardComponentPlugin() {
        super("made a scoreboard");
    }

    @Override
    protected void init() {
        StaticComponentPluginBase.processInitializers(
            StaticComponentPluginBase.getComponentEntrypoints("cardinal-components-scoreboard", ScoreboardComponentInitializer.class),
            initializer -> initializer.registerScoreboardComponentFactories(this)
        );
    }

    @Override
    public <C extends Component> void registerScoreboardComponent(ComponentKey<C> type, ScoreboardComponentFactoryV2<? extends C> factory) {
        this.registerScoreboardComponent(type, type.getComponentClass(), factory);
    }

    @Override
    public <C extends Component> void registerScoreboardComponent(ComponentKey<? super C> type, Class<C> impl, ScoreboardComponentFactoryV2<? extends C> factory) {
        this.checkLoading(ScoreboardComponentFactoryRegistry.class, "registerForScoreboards");
        this.scoreboardFactories.addComponent(type, impl, (scoreboard, server) -> Objects.requireNonNull(((ScoreboardComponentFactoryV2<?>) factory).createForScoreboard(scoreboard, server), "Component factory %s for %s returned null on %s".formatted(factory, type.getId(), scoreboard.getClass().getSimpleName())));
    }

    @Override
    public <C extends Component> void registerTeamComponent(ComponentKey<C> type, TeamComponentFactoryV2<? extends C> factory) {
        this.registerTeamComponent(type, type.getComponentClass(), factory);
    }

    @Override
    public <C extends Component> void registerTeamComponent(ComponentKey<? super C> type, Class<C> impl, TeamComponentFactoryV2<? extends C> factory) {
        this.checkLoading(ScoreboardComponentFactoryRegistry.class, "register");
        this.teamFactories.addComponent(type, impl, (team, scoreboard, server) -> Objects.requireNonNull(((TeamComponentFactoryV2<?>) factory).createForTeam(team, scoreboard, server), "Component factory %s for %s returned null on %s".formatted(factory, type.getId(), scoreboard.getClass().getSimpleName())));
    }
}
