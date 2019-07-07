package nerdhub.cardinal.components.mixins.common.chunk;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.component.extension.CloneableComponent;
import nerdhub.cardinal.components.internal.ChunkAccessor;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk implements Chunk, ComponentProvider, ChunkAccessor {
    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/ProtoChunk;)V", at = @At("RETURN"))
    private void copyFromProto(World world, ProtoChunk proto, CallbackInfo ci) {
        ComponentContainer<CloneableComponent<?>> ourComponents = this.cardinal_getComponentContainer();
        ComponentContainer<CloneableComponent<?>> theirComponents = ((ChunkAccessor)proto).cardinal_getComponentContainer();
        theirComponents.forEach((type, component) -> {
            CloneableComponent<?> copy = component.cloneComponent();
            ourComponents.put(type, copy);
        });
    }
}
