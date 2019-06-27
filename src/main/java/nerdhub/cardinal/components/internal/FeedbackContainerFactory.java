package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.container.ComponentContainer;
import nerdhub.cardinal.components.api.util.component.container.IndexedComponentContainer;

/**
 * A {@code ComponentContainer} factory that takes feedback to optimize
 * future container instantiations.
 */
public final class FeedbackContainerFactory {
    private IndexedComponentContainer model = new IndexedComponentContainer();
    private boolean useFastUtil;
    private int expectedSize;

    public FeedbackContainerFactory() {
        this(false);
    }

    public FeedbackContainerFactory(boolean useFastUtil) {
        this.useFastUtil = useFastUtil;
    }

    /**
     * Creates a new {@code IndexedComponentContainer}.
     * The returned container will be pre-sized based on previous {@link #adjustFrom adjustments}.
     */
    public ComponentContainer create() {
        if (this.useFastUtil) {
            return new FastComponentContainer(this.expectedSize);
        }
        return IndexedComponentContainer.withSettingsFrom(model);
    }

    /**
     * Make this factory adjust its creation settings based on an already initialized
     * value. This method must <strong>NOT</strong> be called with a value that was not
     * {@link #create created} by this factory.
     */
    public void adjustFrom(ComponentContainer initialized) {
        if (initialized instanceof IndexedComponentContainer) {
            IndexedComponentContainer icc = ((IndexedComponentContainer) initialized);
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
