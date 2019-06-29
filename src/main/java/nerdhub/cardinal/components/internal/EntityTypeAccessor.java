package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.minecraft.entity.Entity;

public interface EntityTypeAccessor<E extends Entity> {
    ComponentContainer cardinal_createComponents(E entity);
}
