package nerdhub.cardinal.components.mixins.common.level;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.class_5217;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(class_5217.class)
public interface MixinWorldProperties extends ComponentProvider {
    @Override
    default boolean hasComponent(ComponentType<?> type) {
        throw new UnsupportedOperationException(this.getClass() + " does not implement " + ComponentProvider.class + " from Cardinal-Components-API");
    }

    @Nullable
    @Override
    default <C extends Component> C getComponent(ComponentType<C> type) {
        throw new UnsupportedOperationException(this.getClass() + " does not implement " + ComponentProvider.class + " from Cardinal-Components-API");
    }

    @Override
    default Set<ComponentType<?>> getComponentTypes() {
        throw new UnsupportedOperationException(this.getClass() + " does not implement " + ComponentProvider.class + " from Cardinal-Components-API");
    }
}
