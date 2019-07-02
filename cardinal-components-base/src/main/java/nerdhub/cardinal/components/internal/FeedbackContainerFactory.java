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
package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.ComponentCallback;
import nerdhub.cardinal.components.api.util.container.FastComponentContainer;
import nerdhub.cardinal.components.api.util.container.IndexedComponentContainer;
import net.fabricmc.fabric.api.event.Event;

/**
 * A {@code ComponentContainer} factory that takes feedback to optimize
 * future container instantiations.
 */
public final class FeedbackContainerFactory<T, C extends Component> {
    private IndexedComponentContainer<C> model = new IndexedComponentContainer<>();
    private boolean useFastUtil;
    private int expectedSize;
    private Event<? extends ComponentCallback<T, C>>[] componentEvents;

    @SafeVarargs
    public FeedbackContainerFactory(Event<? extends ComponentCallback<T, C>>... componentEvents) {
        this.componentEvents = componentEvents;
    }

    /**
     * Creates a new {@code IndexedComponentContainer}.
     * The returned container will be pre-sized based on previous {@link #adjustFrom adjustments}.
     */
    public ComponentContainer<C> create(T obj) {
        ComponentContainer<C> components;
        if (this.useFastUtil) {
            components = new FastComponentContainer<>(this.expectedSize);
        } else {
            components = IndexedComponentContainer.withSettingsFrom(model);
        }
        for (Event<? extends ComponentCallback<T, C>> event : this.componentEvents) {
            event.invoker().initComponents(obj, components);
        }
        this.adjustFrom(components);
        return components;
    }

    /**
     * Make this factory adjust its creation settings based on an already initialized
     * value. This method must <strong>NOT</strong> be called with a value that was not
     * {@link #create created} by this factory.
     */
    private void adjustFrom(ComponentContainer<C> initialized) {
        if (initialized instanceof IndexedComponentContainer) {
            IndexedComponentContainer<C> icc = ((IndexedComponentContainer<C>) initialized);
            // If the container was created by this factory, its value range can only be equal or higher to the model.
            // As such, a lower minimum index will always translate to a higher universe size.
            if (model.getUniverseSize() < icc.getUniverseSize()) {
                // Threshold at which the fastutil map becomes more memory efficient
                if (icc.getUniverseSize() > 4 * icc.size()) {
                    this.useFastUtil = true;
                }
                this.model = IndexedComponentContainer.withSettingsFrom(icc);
            }
        }
        this.expectedSize = initialized.size();
    }
}
