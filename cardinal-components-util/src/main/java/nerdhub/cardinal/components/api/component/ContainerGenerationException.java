package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;

public class ContainerGenerationException extends StaticComponentLoadingException {
    public ContainerGenerationException(String message) {
        super(message);
    }

    public ContainerGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
