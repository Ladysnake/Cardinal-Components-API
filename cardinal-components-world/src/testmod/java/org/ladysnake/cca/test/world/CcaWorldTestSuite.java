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
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.ladysnake.cca.test.base.LoadAwareTestComponent;
import org.ladysnake.cca.test.base.Vita;

import java.util.Objects;

public class CcaWorldTestSuite {
    @GameTest
    public void worldLoadWorks(TestContext ctx) {
        ctx.assertEquals(
            1,
            ctx.getWorld().getComponent(LoadAwareTestComponent.KEY).getLoadCounter(),
            Text.literal("Load counter should be incremented once when the world gets loaded -")
        );
        ctx.complete();
    }

    @GameTest
    public void worldSpecificComponentOverrides(TestContext ctx) {
        ctx.waitAndRun(1, () -> {
            ctx.assertEquals(
                666,
                Vita.KEY.getNullable(Objects.requireNonNull(ctx.getWorld().getServer().getWorld(World.NETHER))) instanceof NetherVita v ? v.getVitality() : -1,
                Text.literal("Nether should have its own Vita implementation -")
            );
            ctx.complete();
        });
    }
}
