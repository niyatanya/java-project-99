package hexlet.code.exception;

public class NoPermissionToAccessException extends RuntimeException {
    public NoPermissionToAccessException(String message) {
        super(message);
    }
}
