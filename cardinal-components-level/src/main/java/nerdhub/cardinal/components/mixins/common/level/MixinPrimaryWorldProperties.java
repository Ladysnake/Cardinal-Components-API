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
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.LevelComponentCallback;
import nerdhub.cardinal.components.internal.ComponentsInternals;
import nerdhub.cardinal.components.internal.FeedbackContainerFactory;
import nerdhub.cardinal.components.internal.InternalComponentProvider;
import nerdhub.cardinal.components.internal.StaticLevelComponentPlugin;
import net.minecraft.class_5217;
import net.minecraft.class_5268;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(LevelProperties.class)
public abstract class MixinPrimaryWorldProperties implements class_5268, InternalComponentProvider {
    @Unique
    private static final FeedbackContainerFactory<class_5217, ?> componentContainerFactory
        = ComponentsInternals.createFactory(StaticLevelComponentPlugin.INSTANCE.getFactoryClass(), LevelComponentCallback.EVENT);
    @Unique
    protected ComponentContainer<?> components = componentContainerFactory.create((LevelProperties) (Object) this);

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void readComponents(CompoundTag data, DataFixer fixer, int version, CompoundTag player, CallbackInfo ci) {
        this.components.fromTag(data);
    }

    @Inject(method = "updateProperties", at = @At("RETURN"))
    private void writeComponents(CompoundTag data, CompoundTag player, CallbackInfo ci) {
        this.components.toTag(data);
    }

    @Nonnull
    @Override
    public Object getStaticComponentContainer() {
        return this.components;
    }
}
