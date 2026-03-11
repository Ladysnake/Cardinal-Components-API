/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2026 Ladysnake
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
package org.ladysnake.cca.mixin.level.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.internal.base.ComponentsInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelStorage.class)
public class LevelStorageMixin {
    @WrapOperation(method = "parseSaveProperties", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;readProperties(Lcom/mojang/serialization/Dynamic;Lnet/minecraft/world/level/LevelInfo;Lnet/minecraft/world/level/LevelProperties$SpecialProperty;Lnet/minecraft/world/gen/GeneratorOptions;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/world/level/LevelProperties;"))
    private static LevelProperties readComponents(Dynamic<?> dynamic, LevelInfo info, LevelProperties.SpecialProperty specialProperty, GeneratorOptions generatorOptions, Lifecycle lifecycle, Operation<LevelProperties> original, @Local(argsOnly = true) RegistryWrapper.WrapperLookup registryManager) {
        LevelProperties props = original.call(dynamic, info, specialProperty, generatorOptions, lifecycle);
        NbtCompound nbt = (NbtCompound) dynamic.convert(NbtOps.INSTANCE).getValue();
        try (var errorReporter = new ErrorReporter.Logging(ComponentsInternals.LOGGER)) {
            ((ComponentProvider) props).getComponentContainer().readData(NbtReadView.create(errorReporter, registryManager, nbt));
        }
        return props;
    }
}
