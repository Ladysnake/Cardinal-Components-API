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
package org.ladysnake.cca.test.world;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.ladysnake.cca.test.base.LoadAwareTestComponent;
import org.ladysnake.cca.test.base.Vita;

import java.util.Objects;

public class CcaWorldTestSuite {
    @GameTest
    public void worldLoadWorks(GameTestHelper ctx) {
        ctx.assertValueEqual(
            1,
            ctx.getLevel().getComponent(LoadAwareTestComponent.KEY).getLoadCounter(),
            Component.literal("Load counter should be incremented once when the world gets loaded -")
        );
        ctx.succeed();
    }

    @GameTest
    public void worldSpecificComponentOverrides(GameTestHelper ctx) {
        ctx.runAfterDelay(1, () -> {
            ctx.assertValueEqual(
                666,
                Vita.KEY.getNullable(Objects.requireNonNull(ctx.getLevel().getServer().getLevel(Level.NETHER))) instanceof NetherVita v ? v.getVitality() : -1,
                Component.literal("Nether should have its own Vita implementation -")
            );
            ctx.succeed();
        });
    }
}
