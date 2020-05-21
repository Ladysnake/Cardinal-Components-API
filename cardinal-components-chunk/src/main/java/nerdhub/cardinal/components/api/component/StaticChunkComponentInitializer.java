package nerdhub.cardinal.components.api.component;

public interface StaticChunkComponentInitializer extends StaticComponentInitializer {
    void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry);
}
