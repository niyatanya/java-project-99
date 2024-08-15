package hexlet.code.exception;

public class NoAuthorizationToPerformTheOperation extends RuntimeException {
    public NoAuthorizationToPerformTheOperation(String message) {
        super(message);
    }
}
