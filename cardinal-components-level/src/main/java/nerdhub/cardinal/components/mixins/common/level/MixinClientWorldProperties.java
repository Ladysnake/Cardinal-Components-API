package nerdhub.cardinal.components.mixins.common.level;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.event.LevelComponentCallback;
import nerdhub.cardinal.components.internal.FeedbackContainerFactory;
import net.minecraft.class_5217;
import net.minecraft.class_5269;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

@Mixin(ClientWorld.class_5271.class)
public abstract class MixinClientWorldProperties implements class_5269, ComponentProvider {
    @Unique
    private static final FeedbackContainerFactory<class_5217, ?> componentContainerFactory = new FeedbackContainerFactory<>(LevelComponentCallback.EVENT);
    @Unique
    protected ComponentContainer<?> components = componentContainerFactory.create(this);

    @Override
    public boolean hasComponent(ComponentType<?> type) {
        return this.components.containsKey(type);
    }

    @Nullable
    @Override
    public <C extends Component> C getComponent(ComponentType<C> type) {
        return components.get(type);
    }

    @Override
    public Set<ComponentType<?>> getComponentTypes() {
        return Collections.unmodifiableSet(this.components.keySet());
    }
}
