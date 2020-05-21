package nerdhub.cardinal.components.api.component;

public interface StaticItemComponentInitializer extends StaticComponentInitializer {
    void registerItemComponentFactories(ItemComponentFactoryRegistry registry);
}
