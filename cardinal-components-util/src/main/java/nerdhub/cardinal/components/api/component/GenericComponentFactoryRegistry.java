package nerdhub.cardinal.components.api.component;

import com.google.common.reflect.TypeToken;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * @since 2.4.0
 */
@ApiStatus.Experimental
public interface GenericComponentFactoryRegistry {
    <F> void register(Identifier componentId, Identifier providerId, TypeToken<F> factoryType, F factory);
}
