package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.util.impl.IndexedComponentContainer;

/**
 * A {@code ComponentContainer} factory that takes feedback to optimize
 * future container instanciations.
 */
public final class FeedbackContainerFactory {
    private IndexedComponentContainer model = new IndexedComponentContainer();

    /**
     * Creates a new {@code IndexedComponentContainer}.
     * The returned container will be pre-sized based on previous {@link #adjustFrom adjustments}.
     */
    public IndexedComponentContainer create() {
        return IndexedComponentContainer.withSettingsFrom(model);
    }

    /**
     * Make this factory adjust its creation settings based on an already initialized
     * value. This method must <strong>NOT</strong> be called with a value that was not
     * {@link #create created} by this factory.
     */
    public void adjustFrom(IndexedComponentContainer initialized) {
        // If the container was created by this factory, its value range can only be equal or higher to the model.
        // As such, a lower minimum index will always translate to a higher universe size.
        if (model.getUniverseSize() < initialized.getUniverseSize()) {
            this.model = IndexedComponentContainer.withSettingsFrom(initialized);
        }
    }
}