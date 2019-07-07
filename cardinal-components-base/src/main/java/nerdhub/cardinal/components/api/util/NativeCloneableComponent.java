package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.component.extension.CloneableComponent;

/**
 * A {@link CloneableComponent} implementation that uses java's {@link Cloneable} mechanism for cloning.
 * This removes the need for every subclass to define its own {@code newInstance} implementation.
 */
public interface NativeCloneableComponent extends CloneableComponent, Cloneable {
    /**
     * Creates a brand new instance of this object's class.
     * The returned instance is a shallow copy of this component.
     * <p>
     * Note: this does <strong>not</strong> call the class' constructor!
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
     * The object implementing this method simply needs to call {@code super.clone()}
     */
    Object clone() throws CloneNotSupportedException;
}
