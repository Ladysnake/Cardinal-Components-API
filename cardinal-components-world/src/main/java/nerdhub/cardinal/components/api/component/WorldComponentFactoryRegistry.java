package nerdhub.cardinal.components.api.component;

import net.minecraft.util.Identifier;

public interface WorldComponentFactoryRegistry {
    void register(Identifier componentId, WorldComponentFactory<?> factory);
}
