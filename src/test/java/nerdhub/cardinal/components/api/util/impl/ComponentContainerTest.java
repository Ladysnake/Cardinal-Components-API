package nerdhub.cardinal.components.api.util.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import nerdhub.cardinal.components.TestComponent;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class ComponentContainerTest {
    static List<ComponentType<TestComponent>> testComponentTypes;

    @BeforeAll
    static void setUp() {
        testComponentTypes = IntStream.range(0, 10)
                .mapToObj(Integer::toString)
                .map("test:"::concat)
                .map(Identifier::new)
                .map(id -> ComponentRegistry.INSTANCE.registerIfAbsent(id, TestComponent.class))
                .collect(Collectors.toList());
    }

    @ContainerTest
    void size(ComponentContainer cc) {
        for (int i : new int[] {5, 4, 4, 1, 9, 5}) {
            cc.put(testComponentTypes.get(i), new TestComponent.Impl(i));
        }
        Assertions.assertThrows(NullPointerException.class, () -> cc.put(null, null));
        Assertions.assertThrows(NullPointerException.class, () -> cc.put(null, new TestComponent.Impl(0)));
        Assertions.assertThrows(NullPointerException.class, () -> cc.put(testComponentTypes.get(1), null));
        Assertions.assertEquals(4, cc.size());
    }

    @ContainerTest
    void get(ComponentContainer cc) {
        Map<Integer, Component> indices = new HashMap<>();
        for (int i : new int[] {5, 1, 4, 5}) {
            TestComponent.Impl value = new TestComponent.Impl(i);
            indices.put(i, value);
            cc.put(testComponentTypes.get(i), value);
        }
        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals(indices.containsKey(i), cc.containsKey(testComponentTypes.get(i)));
            Assertions.assertEquals(indices.get(i), cc.get(testComponentTypes.get(i)));
        }
    }

    @ContainerTest
    void serializesCorrectly(ComponentContainer cc, ComponentContainer cc1) {
        for (int i : new int[] {5, 4, 4, 1, 9, 5}) {
            cc.put(testComponentTypes.get(i), new TestComponent.Impl(i));
            cc1.put(testComponentTypes.get(i), new TestComponent.Impl(i+1));
        }
        CompoundTag tag = cc.toTag(new CompoundTag());
        Assertions.assertNotEquals(cc, cc1);
        cc1.fromTag(tag);
        Assertions.assertEquals(cc, cc1);
    }

    @ContainerTest
    void viewsWork(ComponentContainer cc) {
        Map<ComponentType<?>, Component> map = new HashMap<>();
        for (int i : new int[] {5, 4, 4, 1, 9, 5}) {
            cc.put(testComponentTypes.get(i), new TestComponent.Impl(i));
            map.put(testComponentTypes.get(i), new TestComponent.Impl(i));
        }
        Assertions.assertEquals(map.keySet(), cc.keySet());
        Assertions.assertTrue(map.values().containsAll(cc.values()));
        Assertions.assertEquals(map.entrySet(), cc.entrySet());
        Assertions.assertEquals(map, cc);
    }

    static class CustomArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            List<Supplier<ComponentContainer>> vals = ImmutableList.of(IndexedComponentContainer::new, FastComponentContainer::new);
            ImmutableSet<Supplier<ComponentContainer>> types = ImmutableSet.of(IndexedComponentContainer::new, FastComponentContainer::new);
            return Stream.of(types)
                    .flatMap(s -> Sets.cartesianProduct(s, s).stream())
                    .map(l -> l.stream().map(Supplier::get).toArray())
                    .map(Arguments::of);
        }
    }

    @ParameterizedTest(name = "ComponentContainer #{index}")
    @ArgumentsSource(CustomArgumentProvider.class)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ContainerTest {}
}