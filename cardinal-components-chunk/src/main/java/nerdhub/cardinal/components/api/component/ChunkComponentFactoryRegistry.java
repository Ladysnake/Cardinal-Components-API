package nerdhub.cardinal.components.api.component;

import net.minecraft.util.Identifier;

import java.lang.invoke.MethodHandle;

public interface ChunkComponentFactoryRegistry {
    void register(Identifier componentId, MethodHandle factory);
}
