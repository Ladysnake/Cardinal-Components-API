/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2025 Ladysnake
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
package org.ladysnake.cca.mixin.scoreboard;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.internal.base.AbstractComponentContainer;
import org.ladysnake.cca.internal.scoreboard.CcaPackedState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Function;

@Mixin(PlayerTeam.Packed.class)
public abstract class MixinPackedTeam implements CcaPackedState {
    @Unique
    private Optional<CompoundTag> cca$serializedComponents;

    @Override
    public @Nullable CompoundTag cca$getSerializedComponents() {
        return this.cca$serializedComponents.orElse(null);
    }

    @Override
    public void cca$setSerializedComponents(@Nullable CompoundTag nbt) {
        this.cca$serializedComponents = Optional.ofNullable(nbt);
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;", remap = false))
    private static Codec<PlayerTeam.Packed> wrapCodec(Function<RecordCodecBuilder.Instance<PlayerTeam.Packed>, ? extends App<RecordCodecBuilder.Mu<PlayerTeam.Packed>, PlayerTeam.Packed>> builder, Operation<Codec<PlayerTeam.Packed>> original) {
        Codec<PlayerTeam.Packed> baseCodec = original.call(builder);
        MapCodec<PlayerTeam.Packed> baseMapCodec = baseCodec instanceof MapCodec.MapCodecCodec<PlayerTeam.Packed>(
            MapCodec<PlayerTeam.Packed> codec
        ) ? codec : MapCodec.assumeMapUnsafe(baseCodec);
        return RecordCodecBuilder.create(instance -> instance.group(
            baseMapCodec.forGetter(state -> state),
            CompoundTag.CODEC.optionalFieldOf(AbstractComponentContainer.NBT_KEY).forGetter(state -> ((MixinPackedTeam) (Object) state).cca$serializedComponents)
        ).apply(instance, (packed, nbtCompound) -> {
            ((MixinPackedTeam) (Object) packed).cca$serializedComponents = nbtCompound;
            return packed;
        }));
    }
}
