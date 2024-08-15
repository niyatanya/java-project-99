package hexlet.code.exception;

public class EntityCanNotBeDeletedException extends RuntimeException {
    public EntityCanNotBeDeletedException(String message) {
        super(message);
    }
}
