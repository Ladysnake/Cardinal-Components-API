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
package dev.onyxstudios.cca.api.v3.scoreboard;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * A component factory for {@linkplain Scoreboard scoreboards}.
 *
 * <p>When invoked, the factory must return a {@link Component} of the right type.
 *
 * @since 2.4.2
 */
@ApiStatus.Experimental
@FunctionalInterface
public interface ScoreboardComponentFactoryV2<C extends Component> {
    /**
     * Initialize components for the given scoreboard.
     *
     * <p>The component returned by this method will be available
     * on the scoreboard as soon as all component factories have been invoked.
     *
     * @param scoreboard the scoreboard being constructed
     * @param server     the server for which the scoreboard is being constructed, or
     *                   {@code null} if the scoreboard is clientside
     */
    @Contract(pure = true)
    C createForScoreboard(Scoreboard scoreboard, @Nullable MinecraftServer server);
}
