package nerdhub.cardinal.components.api.component;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public interface EntityComponentFactoryRegistry {
    <E extends Entity> void register(Identifier componentId, Class<E> target, EntityComponentFactory<?, E> factory);
}
