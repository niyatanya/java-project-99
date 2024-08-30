package hexlet.code.handler;

import hexlet.code.exception.NoAuthorizationToPerformTheOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(NoAuthorizationToPerformTheOperation.class)
    public ResponseEntity<String> handleNoAuthorizationToPerformTheOperation(NoAuthorizationToPerformTheOperation ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
