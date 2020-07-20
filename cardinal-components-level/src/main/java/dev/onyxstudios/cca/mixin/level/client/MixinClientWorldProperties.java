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
package dev.onyxstudios.cca.mixin.level.client;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.InternalComponentProvider;
import dev.onyxstudios.cca.internal.level.StaticLevelComponentPlugin;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.event.LevelComponentCallback;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Lazy;
import net.minecraft.world.Difficulty;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(ClientWorld.Properties.class)
public abstract class MixinClientWorldProperties implements MutableWorldProperties, InternalComponentProvider {
    @Unique
    private static final Lazy<DynamicContainerFactory<WorldProperties,Component>> componentContainerFactory
        = new Lazy<>(() -> ComponentsInternals.createFactory(StaticLevelComponentPlugin.INSTANCE.getContainerFactoryClass(), LevelComponentCallback.EVENT));
    @Unique
    private ComponentContainer<?> components;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initComponents(Difficulty difficulty, boolean hardcore, boolean flatWorld, CallbackInfo ci) {
        this.components = componentContainerFactory.get().create(this);
    }

    @Nonnull
    @Override
    public ComponentContainer<?> getComponentContainer() {
        return this.components;
    }
}
