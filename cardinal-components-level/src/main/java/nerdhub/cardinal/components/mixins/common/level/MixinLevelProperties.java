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
package nerdhub.cardinal.components.mixins.common.level;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.LevelComponentCallback;
import nerdhub.cardinal.components.internal.ComponentsInternals;
import nerdhub.cardinal.components.internal.DynamicContainerFactory;
import nerdhub.cardinal.components.internal.InternalComponentProvider;
import nerdhub.cardinal.components.internal.StaticLevelComponentPlugin;
import net.minecraft.class_5315;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Lazy;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Mixin(LevelProperties.class)
public abstract class MixinLevelProperties implements ServerWorldProperties, InternalComponentProvider {
    @Unique
    private static final Lazy<DynamicContainerFactory<WorldProperties,?>> componentContainerFactory
        = new Lazy<>(() -> ComponentsInternals.createFactory(StaticLevelComponentPlugin.INSTANCE.getContainerFactoryClass(), LevelComponentCallback.EVENT));
    @Unique
    protected ComponentContainer<?> components = componentContainerFactory.get().create(this);

    @Inject(method = "method_29029", at = @At("RETURN"))
    private static void readComponents(Dynamic<Tag> dynamic, DataFixer dataFixer, int dataVersion, CompoundTag compoundTag, LevelInfo levelInfo, class_5315 arg, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> cir) {
        ((InternalComponentProvider)cir.getReturnValue()).getComponentContainer().fromDynamic(dynamic);
    }

    @Inject(method = "updateProperties", at = @At("RETURN"))
    private void writeComponents(RegistryTracker tracker, CompoundTag data, CompoundTag player, CallbackInfo ci) {
        this.components.toTag(data);
    }

    @Nonnull
    @Override
    public Object getStaticComponentContainer() {
        return this.components;
    }
}
