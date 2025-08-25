package bunny.boardhole.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ValidationErrorResponse {
    private boolean success;
    private String message;
    private Map<String, String> errors;
    
    public static ValidationErrorResponse of(boolean success, String message, Map<String, String> errors) {
        return new ValidationErrorResponse(success, message, errors);
    }
}