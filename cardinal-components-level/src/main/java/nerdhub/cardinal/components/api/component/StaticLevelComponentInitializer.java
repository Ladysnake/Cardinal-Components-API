package nerdhub.cardinal.components.api.component;

public interface StaticLevelComponentInitializer extends StaticComponentInitializer {
    void registerLevelComponentFactories(LevelComponentFactoryRegistry registry);
}
