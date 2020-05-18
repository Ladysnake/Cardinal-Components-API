package nerdhub.cardinal.components.api.component;

import java.lang.invoke.MethodHandles;

public interface StaticEntityComponentInitializer extends StaticComponentInitializer {
    void registerEntityComponentFactories(EntityComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException;
}
