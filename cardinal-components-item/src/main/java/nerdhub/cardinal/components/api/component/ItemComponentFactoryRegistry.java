package nerdhub.cardinal.components.api.component;

import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

public interface ItemComponentFactoryRegistry {
    void register(Identifier componentId, @Nullable Identifier itemId, ItemComponentFactory<?> factory);
}
