package nerdhub.cardinal.components.api.component;

public interface StaticEntityComponentInitializer extends StaticComponentInitializer {
    void registerEntityComponentFactories(EntityComponentFactoryRegistry registry);
}
