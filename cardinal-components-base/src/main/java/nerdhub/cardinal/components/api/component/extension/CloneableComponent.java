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
package nerdhub.cardinal.components.api.component.extension;

import dev.onyxstudios.cca.api.v3.component.Component;
import org.jetbrains.annotations.ApiStatus;

/**
 * A component that can be cloned
 * @see nerdhub.cardinal.components.api.util.NativeCloneableComponent
 * @deprecated use {@link CopyableComponent}
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
public interface CloneableComponent extends Component, CopyableComponent<CloneableComponent> {
    /**
     * Creates a brand new instance of this object's class.
     * There are no guarantees on the state of the returned instance's fields.
     */
    @Deprecated
    CloneableComponent newInstance();

    /**
     * Creates a brand new instance of this object's class that is functionally identical
     * to this component.
     * @return a clone of this component
     * @implSpec this default implementation uses the component's serialization methods
     * to copy properties.
     */
    @Deprecated
    default CloneableComponent cloneComponent() {
        CloneableComponent clone = this.newInstance();
        assert clone.getClass() == this.getClass();
        clone.copyFrom(this);
        return clone;
    }
}
