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
package dev.onyxstudios.cca.internal.base;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComponentRegistryImplTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void checksRegisteredClasses() {
        ComponentRegistryImpl registry = ComponentRegistryImpl.INSTANCE;
        Identifier id = new Identifier("testmod:test");
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.getOrCreate(id, (Class) TestNotComponentItf.class), "Component class must extend Component");
        Assertions.assertDoesNotThrow(() -> registry.getOrCreate(id, TestComponentNotItf.class));
        Assertions.assertDoesNotThrow(() -> registry.getOrCreate(id, TestComponentItf.class));
    }

    @Test
    void doesNotDuplicateComponentTypes() {
        ComponentRegistryImpl registry = ComponentRegistryImpl.INSTANCE;
        Identifier id = new Identifier("testmod:test");
        ComponentKey<?> type = registry.getOrCreate(id, TestComponentItf.class);
        Assertions.assertThrows(IllegalStateException.class, () -> registry.getOrCreate(id, TestComponentItf2.class));
        Assertions.assertThrows(IllegalStateException.class, () -> registry.getOrCreate(id, TestComponentItf3.class));
        Assertions.assertEquals(type, registry.getOrCreate(id, TestComponentItf.class));
        Assertions.assertEquals(1, registry.stream().count());
    }

    @AfterEach
    void tearDown() {
        ComponentRegistryImpl.INSTANCE.clear();
    }

    interface TestNotComponentItf {}

    static class TestComponentNotItf implements Component {
        @Override
        public void readFromNbt(CompoundTag tag) { }

        @Override
        public void writeToNbt(CompoundTag tag) { throw new UnsupportedOperationException(); }
    }

    interface TestComponentItf extends Component {}

    interface TestComponentItf2 extends Component {}

    interface TestComponentItf3 extends TestComponentItf {}
}
