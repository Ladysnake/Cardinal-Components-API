package nerdhub.cardinal.components.api.component;

import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;

public interface ItemComponentFactoryRegistry {
    void register(Identifier componentId, @Nullable Identifier itemId, MethodHandle factory);
}
