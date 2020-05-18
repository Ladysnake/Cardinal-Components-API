package nerdhub.cardinal.components.api.component;

import java.lang.invoke.MethodHandles;

public interface StaticGenericComponentInitializer extends StaticComponentInitializer {
    void registerGenericComponentFactories(GenericComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException;
}
