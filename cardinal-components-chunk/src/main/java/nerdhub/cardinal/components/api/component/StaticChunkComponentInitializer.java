package nerdhub.cardinal.components.api.component;

import java.lang.invoke.MethodHandles;

public interface StaticChunkComponentInitializer extends StaticComponentInitializer {
    void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException;
}
