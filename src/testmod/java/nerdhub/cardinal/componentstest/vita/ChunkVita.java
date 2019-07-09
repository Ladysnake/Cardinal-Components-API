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
package nerdhub.cardinal.componentstest.vita;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.NativeCloneableComponent;
import nerdhub.cardinal.components.api.util.sync.ChunkSyncedComponent;
import nerdhub.cardinal.componentstest.CardinalComponentsTest;
import net.minecraft.world.chunk.Chunk;

// if synchronization was not needed, BaseVita could have been used directly
public class ChunkVita extends BaseVita implements ChunkSyncedComponent {
    private final Chunk chunk;

    public ChunkVita(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void setVitality(int value) {
        super.setVitality(value);
        this.markDirty();
    }

    @Override
    public Chunk getChunk() {
        return this.chunk;
    }

    @Override
    public ComponentType<?> getComponentType() {
        return CardinalComponentsTest.VITA;
    }

    /**
     * Because we use {@link NativeCloneableComponent}, this override is superfluous. Otherwise, it would
     * be required so that the returned object is of the right class.
     */
    @Override
    public ChunkVita newInstance() {
        // Native cloning guarantees the returned object is of the right class
        return (ChunkVita) super.newInstance();
    }
}
