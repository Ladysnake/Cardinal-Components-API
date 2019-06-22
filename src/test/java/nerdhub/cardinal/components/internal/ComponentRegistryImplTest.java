package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComponentRegistryImplTest {

    @SuppressWarnings("unchecked")
    @Test
    void checksRegisteredClasses() {
        ComponentRegistry registry = ComponentRegistry.INSTANCE;
        Identifier id = new Identifier("testmod:test");
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.registerIfAbsent(id, (Class) TestNotComponentNotItf.class), "Component class must be an interface extending Component");
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.registerIfAbsent(id, (Class) TestNotComponentItf.class), "Component class must extend Component");
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.registerIfAbsent(id, TestComponentNotItf.class), "Component class must be an interface");
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

    class TestNotComponentNotItf {}

    interface TestNotComponentItf {}

    class TestComponentNotItf implements Component {}

    interface TestComponentItf extends Component {}

    interface TestComponentItf2 extends Component {}

    interface TestComponentItf3 extends TestComponentItf {}
}