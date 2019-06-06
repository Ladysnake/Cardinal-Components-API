package nerdhub.cardinal.components.api.accessor;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;
import java.util.Set;

public interface EntityComponentAccessor {

    static EntityComponentAccessor get(Entity entity) {
        return (EntityComponentAccessor) entity;
    }

    <T extends Component> boolean hasComponent(ComponentType<T> type);

    @Nullable
    <T extends Component> T getComponent(ComponentType<T> type);

    Set<ComponentType<? extends Component>> getComponentTypes();

}
