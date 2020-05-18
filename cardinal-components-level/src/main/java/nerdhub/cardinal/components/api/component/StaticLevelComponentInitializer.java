package nerdhub.cardinal.components.api.component;

import java.lang.invoke.MethodHandles;

public interface StaticLevelComponentInitializer extends StaticComponentInitializer {
    void registerLevelComponentFactories(LevelComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException;
}
