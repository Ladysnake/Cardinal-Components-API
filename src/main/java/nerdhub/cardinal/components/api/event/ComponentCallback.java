package nerdhub.cardinal.components.api.event;

import nerdhub.cardinal.components.api.component.ComponentContainer;

/**
 * @see EntityComponentCallback
 * @see ItemComponentCallback
 */
@FunctionalInterface
public interface ComponentCallback<T> {
    void initComponents(T object, ComponentContainer components);
}
