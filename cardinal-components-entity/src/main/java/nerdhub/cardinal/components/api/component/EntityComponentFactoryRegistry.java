package nerdhub.cardinal.components.api.component;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.lang.invoke.MethodHandle;

public interface EntityComponentFactoryRegistry {
    void register(Identifier componentId, Class<? extends Entity> target, MethodHandle factory);
}
