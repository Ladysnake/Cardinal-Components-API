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
package org.ladysnake.cca.internal.base;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v8.component.CardinalComponent;
import org.ladysnake.cca.test.base.CardinalGameTest;
import org.ladysnake.elmendorf.ElmendorfTestContext;

import static net.minecraft.network.chat.Component.literal;

public class ComponentRegistryImplTest implements CardinalGameTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @GameTest
    public void checksRegisteredClasses(GameTestHelper ctx) {
        ComponentRegistryImpl registry = ComponentRegistryImpl.INSTANCE;
        ((ElmendorfTestContext) ctx).assertThrows("Component class must extend Component", IllegalArgumentException.class, () -> registry.getOrCreate(CcaTesting.TEST_ID_1, (Class) TestNotComponentItf.class));
        registry.getOrCreate(CcaTesting.TEST_ID_1, TestComponentNotItf.class);
        registry.getOrCreate(CcaTesting.TEST_ID_2, TestComponentItf.class);
    }

    @GameTest
    public void doesNotDuplicateComponentTypes(GameTestHelper ctx) {
        ComponentRegistryImpl registry = ComponentRegistryImpl.INSTANCE;
        Identifier id = CcaTesting.TEST_ID_1;
        ComponentKey<?> type = registry.getOrCreate(id, TestComponentItf.class);
        var elmendorfCtx = ((ElmendorfTestContext) ctx);
        elmendorfCtx.assertThrows(IllegalStateException.class, () -> registry.getOrCreate(id, TestComponentItf2.class));
        elmendorfCtx.assertThrows(IllegalStateException.class, () -> registry.getOrCreate(id, TestComponentItf3.class));
        ctx.assertValueEqual(type, registry.getOrCreate(id, TestComponentItf.class), literal("component key"));
        ctx.assertValueEqual(1L, registry.stream().map(ComponentKey::getId).filter(CcaTesting.ALL_TEST_IDS::contains).count(), literal("number of registrations"));
    }

    @Override
    public void tearDown(GameTestHelper ctx) {
        ctx.succeed();
        for (Identifier id : CcaTesting.ALL_TEST_IDS) {
            ComponentRegistryImpl.INSTANCE.clear(id);
        }
    }

    interface TestNotComponentItf {}

    public static class TestComponentNotItf implements CardinalComponent {
        @Override
        public void readData(ValueInput readView) { }

        @Override
        public void writeData(ValueOutput writeView) { throw new UnsupportedOperationException(); }
    }

    interface TestComponentItf extends CardinalComponent {}

    interface TestComponentItf2 extends CardinalComponent {}

    interface TestComponentItf3 extends TestComponentItf {}
}
