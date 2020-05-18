package nerdhub.cardinal.components.api.component;

import net.minecraft.util.Identifier;

import java.lang.invoke.MethodHandle;

public interface GenericComponentFactoryRegistry {
    void register(Identifier componentId, Identifier providerId, MethodHandle factory);
}
