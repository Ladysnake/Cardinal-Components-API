package nerdhub.cardinal.components.mixins.common.chunk;

import nerdhub.cardinal.components.internal.ChunkAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.village.PointOfInterestStorage;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public abstract class MixinChunkSerializer {
    @Inject(method = "deserialize", at = @At("RETURN"))
    private static void deserialize(ServerWorld world, StructureManager structureManager, PointOfInterestStorage poiStorage, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir) {
        ProtoChunk ret = cir.getReturnValue();
        Chunk chunk = ret instanceof ReadOnlyChunk ? ((ReadOnlyChunk) ret).getWrappedChunk() : ret;
        CompoundTag levelData = tag.getCompound("Level");
        ((ChunkAccessor)chunk).cardinal_getComponentContainer().fromTag(levelData);
    }

    @Inject(method = "serialize", at = @At("RETURN"))
    private static void serialize(ServerWorld world, Chunk chunk, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag ret = cir.getReturnValue();
        CompoundTag levelData = ret.getCompound("Level");
        ((ChunkAccessor)chunk).cardinal_getComponentContainer().toTag(levelData);
    }
}
