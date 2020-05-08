package nerdhub.cardinal.components.internal.asm;

public class StaticComponentLoadingException extends RuntimeException {
    public StaticComponentLoadingException() {
        super();
    }

    public StaticComponentLoadingException(String message) {
        super(message);
    }

    public StaticComponentLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
