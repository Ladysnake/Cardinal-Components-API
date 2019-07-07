package nerdhub.cardinal.components.mixins.common.chunk;

import com.mojang.datafixers.DataFixer;
import nerdhub.cardinal.components.api.event.ChunkSyncCallback;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.VersionedChunkStorage;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage extends VersionedChunkStorage {
    public MixinThreadedAnvilChunkStorage(File file, DataFixer fixer) {
        super(file, fixer);
    }

    @Inject(method = "sendChunkDataPackets", at = @At("RETURN"))
    private void sendChunkComponentsPackets(ServerPlayerEntity player, Packet<?>[] packets, WorldChunk chunk, CallbackInfo ci) {
        ChunkSyncCallback.EVENT.invoker().onChunkSync(player, chunk);
    }
}
