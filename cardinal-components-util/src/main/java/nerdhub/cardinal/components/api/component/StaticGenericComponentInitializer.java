package nerdhub.cardinal.components.api.component;

public interface StaticGenericComponentInitializer extends StaticComponentInitializer {
    void registerGenericComponentFactories(GenericComponentFactoryRegistry registry);
}
