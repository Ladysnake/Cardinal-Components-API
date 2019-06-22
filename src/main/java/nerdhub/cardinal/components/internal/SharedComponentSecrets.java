package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.ComponentType;

public final class SharedComponentSecrets {
    static ComponentType<?>[] registeredComponents = new ComponentType[0];

    public static ComponentType<?>[] getRegisteredComponents() {
        return registeredComponents;
    }

}