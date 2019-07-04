package nerdhub.cardinal.components.mixins.common.world;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.event.WorldComponentCallback;
import nerdhub.cardinal.components.internal.FeedbackContainerFactory;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;

@Mixin(World.class)
public abstract class MixinWorld implements ComponentProvider {
    @Unique
    private static final FeedbackContainerFactory<World, ?> componentContainerFactory = new FeedbackContainerFactory<>(WorldComponentCallback.EVENT);
    @Unique
    protected ComponentContainer<?> components;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initComponents(LevelProperties props, DimensionType dim, BiFunction<World, Dimension, ChunkManager> managerFactory, Profiler profiler, boolean client, CallbackInfo ci) {
        this.components = componentContainerFactory.create((World) (Object) this);
    }

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
