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
package nerdhub.cardinal.components.api.util.component;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.extension.CloneableComponent;
import net.minecraft.nbt.CompoundTag;

/**
 * A component that has no inherent properties and is only used to indicate the existence
 * of a property on an object.
 */
public interface MarkerComponent extends Component, CloneableComponent<MarkerComponent> {
    MarkerComponent INSTANCE = new Impl();

    /**
     * Marker components are all equal to each other.
     */
    @Override
    boolean isComponentEqual(Component other);

    final class Impl implements MarkerComponent {

        @Override
        public void fromTag(CompoundTag tag) {
            // NO-OP
        }

        @Override
        public CompoundTag toTag(CompoundTag tag) {
            return tag;
        }

        @Override
        public MarkerComponent newInstance() {
            return INSTANCE;
        }

        @Override
        public boolean isComponentEqual(Component other) {
            return other instanceof MarkerComponent;
        }
    }
}
