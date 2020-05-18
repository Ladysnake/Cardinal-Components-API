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
package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.ComponentCallback;
import nerdhub.cardinal.components.api.util.container.FastComponentContainer;
import net.fabricmc.fabric.api.event.Event;

import javax.annotation.Nonnull;

/**
 * A {@code ComponentContainer} factory that takes feedback to optimize
 * future container instantiations.
 */
public class FeedbackContainerFactory<T, C extends Component> {
    private int expectedSize = 0;
    private final Event<? extends ComponentCallback<T, C>>[] componentEvents;

    @SafeVarargs
    public FeedbackContainerFactory(Event<? extends ComponentCallback<T, C>>... componentEvents) {
        this.componentEvents = componentEvents;
    }

    /**
     * Creates a new {@code IndexedComponentContainer}.
     * The returned container will be pre-sized based on previous {@link #adjustFrom adjustments}.
     */
    public ComponentContainer<C> create(T obj) {
        FastComponentContainer<C> components = this.createContainer(this.expectedSize, obj);
        for (Event<? extends ComponentCallback<T, C>> event : this.componentEvents) {
            event.invoker().initComponents(obj, components);
        }
        this.adjustFrom(components);
        return components;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})   // overridden by generated classes
    @Nonnull
    protected FastComponentContainer<C> createContainer(int expectedSize, T obj) {
        return new FastComponentContainer<>(expectedSize);
    }

    /**
     * Make this factory adjust its creation settings based on an already initialized
     * value. This method must <strong>NOT</strong> be called with a value that was not
     * {@link #create created} by this factory.
     */
    private void adjustFrom(FastComponentContainer<C> initialized) {
        this.expectedSize = initialized.dynamicSize();
    }
}
