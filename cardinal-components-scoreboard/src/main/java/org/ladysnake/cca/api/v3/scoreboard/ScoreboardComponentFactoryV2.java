/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2026 Ladysnake
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
package org.ladysnake.cca.api.v3.scoreboard;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v8.component.CardinalComponent;
import org.ladysnake.cca.internal.base.asm.CalledByAsm;

/**
 * A component factory for {@linkplain Scoreboard scoreboards}.
 *
 * <p>When invoked, the factory must return a {@link CardinalComponent} of the right type.
 *
 * @since 2.7.10
 */
@FunctionalInterface
public interface ScoreboardComponentFactoryV2<C extends CardinalComponent> {
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
    @CalledByAsm
    @Contract(pure = true)
    C createForScoreboard(Scoreboard scoreboard, @Nullable MinecraftServer server);
}
