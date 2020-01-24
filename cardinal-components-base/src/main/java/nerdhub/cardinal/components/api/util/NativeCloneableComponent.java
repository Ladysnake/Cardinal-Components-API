/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 GlassPane
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
package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.component.extension.CloneableComponent;

/**
 * A {@link CloneableComponent} implementation that uses java's {@link Cloneable} mechanism for cloning.
 * This removes the need for every subclass to define its own {@code newInstance} implementation.
 * @deprecated use {@link nerdhub.cardinal.components.api.component.extension.CopyableComponent}
 */
@Deprecated
public interface NativeCloneableComponent extends CloneableComponent, Cloneable {
    /**
     * Creates a brand new instance of this object's class using the {@link #clone()}
     * method.
     *
     * <p> Note: this does <strong>not</strong> call the class' constructor!
     * Components depending on specific initialization code should consider
     * using the more generic {@code CloneableComponent} interface.
     *
     * @implNote The default implementation creates a shallow copy of this component.
     */
    @Override
    default CloneableComponent newInstance() {
        try {
            return (CloneableComponent) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @implNote The object implementing this method simply needs to call {@code super.clone()}
     */
    Object clone() throws CloneNotSupportedException;
}
