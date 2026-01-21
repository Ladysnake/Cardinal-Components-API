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
package org.ladysnake.cca.test.scoreboard;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreboardSaveData;
import org.ladysnake.cca.api.v3.component.ComponentAccess;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.test.base.CardinalGameTest;
import org.ladysnake.cca.test.base.LoadAwareTestComponent;
import org.ladysnake.cca.test.base.Vita;
import org.ladysnake.elmendorf.ElmendorfTestContext;

public class CcaScoreboardTestSuite implements CardinalGameTest {

    public static final String TEST_TEAM_NAME = "cca-team-test";

    @GameTest
    public void serverLoadWorks(GameTestHelper ctx) {
        ctx.assertValueEqual(
            ((ComponentProvider) ctx.getLevel().getScoreboard()).getComponent(LoadAwareTestComponent.KEY).getLoadCounter(), 1,
            Component.literal("Load counter should be incremented once when the server gets loaded -")
        );
        ctx.succeed();
    }

    @GameTest
    public void componentSerializesCorrectly(GameTestHelper ctx) {
        ServerScoreboard scoreboard = ctx.getLevel().getScoreboard();
        ((ComponentProvider) scoreboard).getComponent(Vita.KEY).setVitality(42);
        PlayerTeam testTeam = scoreboard.addPlayerTeam(TEST_TEAM_NAME);
        ((ComponentProvider) testTeam).getComponent(Vita.KEY).setVitality(420);
        ScoreboardSaveData state = ScoreboardSaveData.TYPE.constructor().get();
        scoreboard.storeToSaveDataIfDirty(state);
        ((ComponentProvider) scoreboard).getComponent(Vita.KEY).setVitality(0);
        scoreboard.removePlayerTeam(testTeam);
        ctx.assertValueEqual(
            0, ((ComponentProvider) scoreboard).getComponent(Vita.KEY).getVitality(),
            Component.literal("reset vita")
        );
        ((ElmendorfTestContext) ctx).assertTrue("Reset team should be null", scoreboard.getPlayerTeam(TEST_TEAM_NAME) == null);
        scoreboard.load(state.getData());
        ctx.assertValueEqual(
            42, ((ComponentProvider) scoreboard).getComponent(Vita.KEY).getVitality(),
            Component.literal("deserialized vita")
        );
        PlayerTeam deserializedTeam = scoreboard.getPlayerTeam(TEST_TEAM_NAME);
        ((ElmendorfTestContext) ctx).assertFalse("Deserialized team should not be null", deserializedTeam == null);
        assert deserializedTeam != null;
        ctx.assertValueEqual(
            420, ((ComponentAccess) deserializedTeam).getComponent(Vita.KEY).getVitality(),
            Component.literal("deserialized vita")
        );
        ctx.succeed();
    }
}
