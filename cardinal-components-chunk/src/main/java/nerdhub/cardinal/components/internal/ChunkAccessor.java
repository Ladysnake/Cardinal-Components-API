package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.extension.CloneableComponent;

public interface ChunkAccessor {
    ComponentContainer<CloneableComponent<?>> cardinal_getComponentContainer();
}
