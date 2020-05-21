package nerdhub.cardinal.components.api.component;

public interface StaticWorldComponentInitializer extends StaticComponentInitializer {
    void registerWorldComponentFactories(WorldComponentFactoryRegistry registry);
}
