/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 Ladysnake
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

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.util.Identifier;
import org.junit.Assert;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.internal.base.asm.StaticComponentLoadingException;
import org.ladysnake.cca.test.base.CardinalGameTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QualifiedComponentFactoryTest implements CardinalGameTest {
    @Override
    public void tearDown() {
        for (Identifier id : CcaTesting.ALL_TEST_IDS) {
            ComponentRegistryImpl.INSTANCE.clear(id);
        }
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void sortKeepsOrderByDefault() {
        Map<ComponentKey<?>, QualifiedComponentFactory<Object>> map = new LinkedHashMap<>();
        var key1 = ComponentRegistry.getOrCreate(CcaTesting.TEST_ID_1, ComponentRegistryImplTest.TestComponentNotItf.class);
        var key2 = ComponentRegistry.getOrCreate(CcaTesting.TEST_ID_2, ComponentRegistryImplTest.TestComponentNotItf.class);
        var key3 = ComponentRegistry.getOrCreate(CcaTesting.TEST_ID_3, ComponentRegistryImplTest.TestComponentNotItf.class);
        map.put(key1, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of()));
        map.put(key2, new QualifiedComponentFactory<>(new Object(), key2.getComponentClass(), Set.of()));
        map.put(key3, new QualifiedComponentFactory<>(new Object(), key3.getComponentClass(), Set.of()));
        Map<ComponentKey<?>, QualifiedComponentFactory<Object>> sorted = QualifiedComponentFactory.sort(map);
        Assert.assertNotSame(map, sorted);
        Assert.assertEquals(List.copyOf(map.keySet()), List.copyOf(sorted.keySet()));
        map = new LinkedHashMap<>();
        map.put(key1, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of()));
        map.put(key3, new QualifiedComponentFactory<>(new Object(), key3.getComponentClass(), Set.of()));
        map.put(key2, new QualifiedComponentFactory<>(new Object(), key2.getComponentClass(), Set.of()));
        sorted = QualifiedComponentFactory.sort(map);
        Assert.assertNotSame(map, sorted);
        Assert.assertEquals(List.copyOf(map.keySet()), List.copyOf(sorted.keySet()));
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void sortThrowsOnUnsatisfiedDependency() {
        Map<ComponentKey<?>, QualifiedComponentFactory<Object>> map = new LinkedHashMap<>();
        var key1 = ComponentRegistry.getOrCreate(CcaTesting.TEST_ID_1, ComponentRegistryImplTest.TestComponentNotItf.class);
        var key2 = ComponentRegistry.getOrCreate(CcaTesting.TEST_ID_2, ComponentRegistryImplTest.TestComponentNotItf.class);
        map.put(key1, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of(key2)));
        Assert.assertThrows(StaticComponentLoadingException.class, () -> QualifiedComponentFactory.checkDependenciesSatisfied(map));
        map.put(key2, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of()));
        QualifiedComponentFactory.checkDependenciesSatisfied(map);
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void sortThrowsOnCircularDependency() {
        Map<ComponentKey<?>, QualifiedComponentFactory<Object>> map = new LinkedHashMap<>();
        var key1 = ComponentRegistry.getOrCreate(CcaTesting.TEST_ID_1, ComponentRegistryImplTest.TestComponentNotItf.class);
        var key2 = ComponentRegistry.getOrCreate(CcaTesting.TEST_ID_2, ComponentRegistryImplTest.TestComponentNotItf.class);
        var key3 = ComponentRegistry.getOrCreate(CcaTesting.TEST_ID_3, ComponentRegistryImplTest.TestComponentNotItf.class);
        map.put(key1, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of(key1)));
        Assert.assertThrows(StaticComponentLoadingException.class, () -> QualifiedComponentFactory.sort(map));
        map.put(key1, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of(key2)));
        map.put(key2, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of(key1)));
        Assert.assertThrows(StaticComponentLoadingException.class, () -> QualifiedComponentFactory.sort(map));
        map.put(key1, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of(key2)));
        map.put(key2, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of(key3)));
        map.put(key3, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of(key1)));
        Assert.assertThrows(StaticComponentLoadingException.class, () -> QualifiedComponentFactory.sort(map));
        map.put(key3, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of()));
        QualifiedComponentFactory.sort(map);
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void sortRespectsDependencyOrdering() {
        Map<ComponentKey<?>, QualifiedComponentFactory<Object>> map = new LinkedHashMap<>();
        var key1 = ComponentRegistry.getOrCreate(CcaTesting.TEST_ID_1, ComponentRegistryImplTest.TestComponentNotItf.class);
        var key2 = ComponentRegistry.getOrCreate(CcaTesting.TEST_ID_2, ComponentRegistryImplTest.TestComponentNotItf.class);
        var key3 = ComponentRegistry.getOrCreate(CcaTesting.TEST_ID_3, ComponentRegistryImplTest.TestComponentNotItf.class);
        map.put(key1, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of(key2)));
        map.put(key2, new QualifiedComponentFactory<>(new Object(), key2.getComponentClass(), Set.of()));
        Assert.assertEquals(List.of(key2, key1), List.copyOf(QualifiedComponentFactory.sort(map).keySet()));
        map.put(key1, new QualifiedComponentFactory<>(new Object(), key1.getComponentClass(), Set.of(key2, key3)));
        map.put(key3, new QualifiedComponentFactory<>(new Object(), key3.getComponentClass(), Set.of(key2)));
        Assert.assertEquals(List.of(key2, key3, key1), List.copyOf(QualifiedComponentFactory.sort(map).keySet()));
    }
}
