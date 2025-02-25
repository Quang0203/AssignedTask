package cviettel.loginservice.configuration.keycloack.exception;

public class CustomKeycloakException extends RuntimeException {
    public CustomKeycloakException(String message) {
        super(message);
    }

    public CustomKeycloakException(String message, Throwable cause) {
        super(message, cause);
    }
}
