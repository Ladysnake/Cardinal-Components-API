package nerdhub.cardinal.components.api.component;

import net.minecraft.util.Identifier;

public interface LevelComponentFactoryRegistry {
    void register(Identifier componentId, LevelComponentFactory<?> factory);
}
