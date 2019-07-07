/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
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
package nerdhub.cardinal.components.mixins.common.chunk;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.component.extension.CloneableComponent;
import nerdhub.cardinal.components.internal.ChunkAccessor;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(ReadOnlyChunk.class)
public abstract class MixinReadOnlyChunk extends ProtoChunk implements ComponentProvider, ChunkAccessor {
    @Shadow @Final private WorldChunk wrapped;

    public MixinReadOnlyChunk(ChunkPos pos, UpgradeData data) {
        super(pos, data);
    }

    @Override
    public boolean hasComponent(ComponentType<?> type) {
        return ((ComponentProvider) this.wrapped).hasComponent(type);
    }

    @Nullable
    @Override
    public <C extends Component> C getComponent(ComponentType<C> type) {
        return ((ComponentProvider) this.wrapped).getComponent(type);
    }

    @Override
    public Set<ComponentType<?>> getComponentTypes() {
        return ((ComponentProvider) this.wrapped).getComponentTypes();
    }

    @Override
    public ComponentContainer<CloneableComponent> cardinal_getComponentContainer() {
        return ((ChunkAccessor) this.wrapped).cardinal_getComponentContainer();
    }
}
