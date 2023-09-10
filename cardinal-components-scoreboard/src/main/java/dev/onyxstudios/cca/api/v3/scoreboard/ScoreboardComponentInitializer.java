/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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

import dev.onyxstudios.cca.internal.base.ComponentRegistrationInitializer;

/**
 * Entrypoint getting invoked to register <em>static</em> team component factories.
 *
 * <p>The entrypoint is exposed as either {@code "cardinal-components"} or {@code "cardinal-components-scoreboard"} in the mod json and runs for any environment.
 * It usually executes right before the first {@link net.minecraft.scoreboard.Scoreboard} instance is created.
 *
 * @since 2.4.2
 */
public interface ScoreboardComponentInitializer extends ComponentRegistrationInitializer {
    /**
     * Called to register component factories for statically declared component types.
     *
     * <p><strong>The passed registry must not be held onto!</strong> Static component factories
     * must not be registered outside of this method.
     *
     * @param registry a {@link ScoreboardComponentFactoryRegistry} for <em>statically declared</em> components
     */
    void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry);
}
