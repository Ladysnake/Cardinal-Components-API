package nerdhub.cardinal.components.internal;

public final class SharedComponentSecrets {
    private static ComponentType<?>[] registeredComponents;

    public static ComponentType<?>[] getRegisteredComponents() {
        return registeredComponents;
    }

}