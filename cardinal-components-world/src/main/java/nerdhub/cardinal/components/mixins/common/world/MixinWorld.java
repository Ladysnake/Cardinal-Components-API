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
package nerdhub.cardinal.components.mixins.common.world;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.WorldComponentCallback;
import nerdhub.cardinal.components.internal.ComponentsInternals;
import nerdhub.cardinal.components.internal.FeedbackContainerFactory;
import nerdhub.cardinal.components.internal.InternalComponentProvider;
import nerdhub.cardinal.components.internal.world.StaticWorldComponentPlugin;
import net.minecraft.class_5269;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Mixin(World.class)
public abstract class MixinWorld implements InternalComponentProvider {
    @Unique
    private static final FeedbackContainerFactory<World, ?> componentContainerFactory
        = ComponentsInternals.createFactory(StaticWorldComponentPlugin.INSTANCE.getFactoryClass(), WorldComponentCallback.EVENT);

    @Unique
    protected ComponentContainer<?> components;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initComponents(class_5269 levelProperties, DimensionType dimensionType, BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Supplier<Profiler> profiler, boolean isClient, CallbackInfo ci) {
        this.components = componentContainerFactory.create((World) (Object) this);
    }

    @Nonnull
    @Override
    public Object getStaticComponentContainer() {
        return this.components;
    }
}
