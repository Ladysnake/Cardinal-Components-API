package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.ComponentType;

public final class SharedComponentSecrets {
    private static ComponentType<?>[] registeredComponents;

    public static ComponentType<?>[] getRegisteredComponents() {
        return registeredComponents;
    }

}