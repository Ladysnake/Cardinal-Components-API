package nerdhub.cardinal.components.api.component;

import java.lang.invoke.MethodHandles;

public interface StaticItemComponentInitializer extends StaticComponentInitializer {
    void registerItemComponentFactories(ItemComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException;
}
