/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2022 OnyxStudios
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
package dev.onyxstudios.cca.internal.base;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.test.base.CardinalGameTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.util.Identifier;
import org.junit.Assert;

public class ComponentRegistryImplTest implements CardinalGameTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void checksRegisteredClasses() {
        ComponentRegistryImpl registry = ComponentRegistryImpl.INSTANCE;
        Assert.assertThrows("Component class must extend Component", IllegalArgumentException.class, () -> registry.getOrCreate(CcaTesting.TEST_ID_1, (Class) TestNotComponentItf.class));
        registry.getOrCreate(CcaTesting.TEST_ID_1, TestComponentNotItf.class);
        registry.getOrCreate(CcaTesting.TEST_ID_2, TestComponentItf.class);
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void doesNotDuplicateComponentTypes() {
        ComponentRegistryImpl registry = ComponentRegistryImpl.INSTANCE;
        Identifier id = CcaTesting.TEST_ID_1;
        ComponentKey<?> type = registry.getOrCreate(id, TestComponentItf.class);
        Assert.assertThrows(IllegalStateException.class, () -> registry.getOrCreate(id, TestComponentItf2.class));
        Assert.assertThrows(IllegalStateException.class, () -> registry.getOrCreate(id, TestComponentItf3.class));
        Assert.assertEquals(type, registry.getOrCreate(id, TestComponentItf.class));
        Assert.assertEquals(1, registry.stream().map(ComponentKey::getId).filter(CcaTesting.ALL_TEST_IDS::contains).count());
    }

    @Override
    public void tearDown() {
        for (Identifier id : CcaTesting.ALL_TEST_IDS) {
            ComponentRegistryImpl.INSTANCE.clear(id);
        }
    }

    interface TestNotComponentItf {}

    public static class TestComponentNotItf implements Component {
        @Override
        public void readFromNbt(NbtCompound tag) { }

        @Override
        public void writeToNbt(NbtCompound tag) { throw new UnsupportedOperationException(); }
    }

    interface TestComponentItf extends Component {}

    interface TestComponentItf2 extends Component {}

    interface TestComponentItf3 extends TestComponentItf {}
}
