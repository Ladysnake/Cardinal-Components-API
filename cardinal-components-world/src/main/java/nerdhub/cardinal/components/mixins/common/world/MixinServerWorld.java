package nerdhub.cardinal.components.mixins.common.world;

import nerdhub.cardinal.components.internal.world.ComponentPersistentState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends MixinWorld {
    @Shadow public abstract PersistentStateManager getPersistentStateManager();

    @Unique
    private static final String PERSISTENT_STATE_KEY = "cardinal_world_components";

    @Inject(at = @At("RETURN"), method = "<init>")
    private void constructor(MinecraftServer server, Executor executor, WorldSaveHandler oldWorldSaveHandler, LevelProperties levelProperties, DimensionType dimensionType, Profiler profiler, WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        PersistentStateManager persistentStateManager = this.getPersistentStateManager();
        persistentStateManager.getOrCreate(() -> new ComponentPersistentState(PERSISTENT_STATE_KEY, this.components), PERSISTENT_STATE_KEY);
    }

}
