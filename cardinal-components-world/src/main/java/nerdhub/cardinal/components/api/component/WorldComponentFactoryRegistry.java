package nerdhub.cardinal.components.api.component;

import net.minecraft.util.Identifier;

import java.lang.invoke.MethodHandle;

public interface WorldComponentFactoryRegistry {
    void register(Identifier componentId, MethodHandle factory);
}
