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

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComponentRegistryImplTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void checksRegisteredClasses() {
        ComponentRegistry registry = ComponentRegistry.INSTANCE;
        Identifier id = new Identifier("testmod:test");
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.registerIfAbsent(id, (Class) TestNotComponentItf.class), "Component class must extend Component");
        Assertions.assertDoesNotThrow(() -> registry.registerIfAbsent(id, TestComponentNotItf.class));
        Assertions.assertDoesNotThrow(() -> registry.registerIfAbsent(id, TestComponentItf.class));
    }

    @Test
    void doesNotDuplicateComponentTypes() {
        ComponentRegistry registry = ComponentRegistry.INSTANCE;
        Identifier id = new Identifier("testmod:test");
        ComponentType<?> type = registry.registerIfAbsent(id, TestComponentItf.class);
        Assertions.assertThrows(IllegalStateException.class, () -> registry.registerIfAbsent(id, TestComponentItf2.class));
        Assertions.assertThrows(IllegalStateException.class, () -> registry.registerIfAbsent(id, TestComponentItf3.class));
        Assertions.assertEquals(type, registry.registerIfAbsent(id, TestComponentItf.class));
        Assertions.assertEquals(1, registry.stream().count());
    }

    @AfterEach
    void tearDown() {
        ((ComponentRegistryImpl) ComponentRegistry.INSTANCE).clear();
    }

    interface TestNotComponentItf {}

    static class TestComponentNotItf implements Component {
        @Override
        public void fromTag(CompoundTag tag) { }

        @Override
        public CompoundTag toTag(CompoundTag tag) { throw new UnsupportedOperationException(); }
    }

    interface TestComponentItf extends Component {}

    interface TestComponentItf2 extends Component {}

    interface TestComponentItf3 extends TestComponentItf {}
}
