package nerdhub.cardinal.components.api.component;

import net.minecraft.util.Identifier;

import java.lang.invoke.MethodHandle;

public interface LevelComponentFactoryRegistry {
    void register(Identifier componentId, MethodHandle factory);
}
