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
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

/**
 * A component that stores data in a {@link CompoundTag}.
 */
public interface NbtStorageComponent extends Component {

    CompoundTag getData();

    final class Impl implements NbtStorageComponent {
        private CompoundTag data = new CompoundTag();

        @Override
        public CompoundTag getData() {
            return data;
        }

        @Override
        public void fromTag(CompoundTag tag) {
            this.data = tag.getCompound("data");
        }

        @Override
        public CompoundTag toTag(CompoundTag tag) {
            tag.put("data", this.data);
            return tag;
        }

        @Override
        public NbtStorageComponent newInstance() {
            return new Impl();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Impl impl = (Impl) o;
            return Objects.equals(data, impl.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }
    }
}
