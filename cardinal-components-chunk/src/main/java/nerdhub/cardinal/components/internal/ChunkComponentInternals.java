package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.event.ChunkComponentCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

public final class ChunkComponentInternals {
    public static final FeedbackContainerFactory<Chunk, CopyableComponent<?>> componentContainerFactory = createFactory();

    @Nonnull
    private static FeedbackContainerFactory<Chunk, CopyableComponent<?>> createFactory() {
        Class<? extends FeedbackContainerFactory<?, ?>> factoryClass = StaticChunkComponentPlugin.INSTANCE.getFactoryClass();
        try {
            @SuppressWarnings("unchecked") FeedbackContainerFactory<Chunk, CopyableComponent<?>> ret = (FeedbackContainerFactory<Chunk, CopyableComponent<?>>) factoryClass.getConstructor(Event[].class).newInstance((Object) new Event[]{ChunkComponentCallback.EVENT});
            return ret;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to instantiate generated component factory", e);
        }
    }
}
