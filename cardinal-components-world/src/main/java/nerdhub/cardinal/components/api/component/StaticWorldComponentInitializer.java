package nerdhub.cardinal.components.api.component;

import java.lang.invoke.MethodHandles;

public interface StaticWorldComponentInitializer extends StaticComponentInitializer {
    void registerWorldComponentFactories(WorldComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException;
}
