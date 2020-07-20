/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 OnyxStudios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dev.onyxstudios.cca.mixin.chunk.common;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.InternalComponentProvider;
import dev.onyxstudios.cca.internal.chunk.StaticChunkComponentPlugin;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.event.ChunkComponentCallback;
import net.minecraft.util.Lazy;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk implements Chunk, InternalComponentProvider {
    @Unique
    private static final Lazy<DynamicContainerFactory<Chunk, CopyableComponent<?>>> componentsContainerFactory
        = new Lazy<>(() -> ComponentsInternals.createFactory(StaticChunkComponentPlugin.INSTANCE.getContainerFactoryClass(), ChunkComponentCallback.EVENT));
    @Unique
    private ComponentContainer<CopyableComponent<?>> components;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/biome/source/BiomeArray;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/TickScheduler;Lnet/minecraft/world/TickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Ljava/util/function/Consumer;)V", at = @At("RETURN"))
    private void initComponents(CallbackInfo ci) {
        this.components = componentsContainerFactory.get().create(this);
    }

    @Nonnull
    @Override
    public ComponentContainer<?> getComponentContainer() {
        return this.components;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/ProtoChunk;)V", at = @At("RETURN"))
    private <C extends CopyableComponent<C>> void copyFromProto(World world, ProtoChunk proto, CallbackInfo ci) {
        for (ComponentKey<?> key : this.components.keys()) {
            @SuppressWarnings("unchecked") C theirs = (C) key.getNullable(proto);

            if (theirs != null) {
                @SuppressWarnings("unchecked") C ours = (C) key.get(this);
                ours.copyFrom(theirs);
            }
        }
    }
}
