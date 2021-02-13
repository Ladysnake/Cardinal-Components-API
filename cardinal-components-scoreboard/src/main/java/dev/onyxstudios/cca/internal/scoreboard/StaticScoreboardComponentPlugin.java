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

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentFactoryV2;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import dev.onyxstudios.cca.api.v3.scoreboard.TeamComponentFactoryV2;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.CcaAsmHelper;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Lazy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class StaticScoreboardComponentPlugin extends LazyDispatcher implements ScoreboardComponentFactoryRegistry {
    public static final StaticScoreboardComponentPlugin INSTANCE = new StaticScoreboardComponentPlugin();
    public static final Lazy<ScoreboardComponentContainerFactory> scoreboardComponentsContainerFactory
        = new Lazy<>(INSTANCE::buildScoreboardContainerFactory);
    public static final Lazy<TeamComponentContainerFactory> teamComponentsContainerFactory
        = new Lazy<>(INSTANCE::buildTeamContainerFactory);

    private final Map<ComponentKey<?>, ScoreboardComponentFactoryV2<?>> scoreboardFactories = new LinkedHashMap<>();
    private final Map<ComponentKey<?>, Class<? extends Component>> scoreboardComponentImpls = new LinkedHashMap<>();
    private final Map<ComponentKey<?>, TeamComponentFactoryV2<?>> teamFactories = new LinkedHashMap<>();
    private final Map<ComponentKey<?>, Class<? extends Component>> teamComponentImpls = new LinkedHashMap<>();

    private ScoreboardComponentContainerFactory buildScoreboardContainerFactory() {
        this.ensureInitialized();

        if (this.scoreboardFactories.isEmpty()) {
            return (sc, s) -> ComponentContainer.EMPTY;
        }

        try {
            String implNameSuffix = "ScoreboardImpl";
            Class<? extends ComponentContainer> containerClass = CcaAsmHelper.spinComponentContainer(
                ScoreboardComponentFactoryV2.class, this.scoreboardFactories, this.scoreboardComponentImpls, implNameSuffix
            );
            Class<? extends ScoreboardComponentContainerFactory> factoryClass = StaticComponentPluginBase.spinContainerFactory(
                implNameSuffix, ScoreboardComponentContainerFactory.class, containerClass, Scoreboard.class, MinecraftServer.class
            );
            return ComponentsInternals.createFactory(factoryClass);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TeamComponentContainerFactory buildTeamContainerFactory() {
        this.ensureInitialized();

        if (this.teamFactories.isEmpty()) {
            return (t, sc, s) -> ComponentContainer.EMPTY;
        }

        try {
            String implNameSuffix = "TeamImpl";
            Class<? extends ComponentContainer> containerClass = CcaAsmHelper.spinComponentContainer(
                TeamComponentFactoryV2.class, this.teamFactories, this.teamComponentImpls, implNameSuffix
            );
            Class<? extends TeamComponentContainerFactory> factoryClass = StaticComponentPluginBase.spinContainerFactory(
                implNameSuffix, TeamComponentContainerFactory.class, containerClass, Team.class, Scoreboard.class, MinecraftServer.class
            );
            return ComponentsInternals.createFactory(factoryClass);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        this.scoreboardFactories.put(type, (scoreboard, server) -> Objects.requireNonNull(((ScoreboardComponentFactoryV2<?>) factory).createForScoreboard(scoreboard, server), "Component factory " + factory + " for " + type.getId() + " returned null on " + scoreboard.getClass().getSimpleName()));
        this.scoreboardComponentImpls.put(type, impl);
    }

    @Override
    public <C extends Component> void registerTeamComponent(ComponentKey<C> type, TeamComponentFactoryV2<? extends C> factory) {
        this.registerTeamComponent(type, type.getComponentClass(), factory);
    }

    @Override
    public <C extends Component> void registerTeamComponent(ComponentKey<? super C> type, Class<C> impl, TeamComponentFactoryV2<? extends C> factory) {
        this.checkLoading(ScoreboardComponentFactoryRegistry.class, "register");
        this.teamFactories.put(type, (team, scoreboard, server) -> Objects.requireNonNull(((TeamComponentFactoryV2<?>) factory).createForTeam(team, scoreboard, server), "Component factory " + factory + " for " + type.getId() + " returned null on " + scoreboard.getClass().getSimpleName()));
        this.teamComponentImpls.put(type, impl);
    }
}
