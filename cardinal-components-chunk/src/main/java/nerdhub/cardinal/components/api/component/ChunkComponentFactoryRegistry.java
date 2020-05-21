package nerdhub.cardinal.components.api.component;

import net.minecraft.util.Identifier;

public interface ChunkComponentFactoryRegistry {
    void register(Identifier componentId, ChunkComponentFactory<?> factory);
}
