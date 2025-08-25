package bunny.boardhole.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String message;
    
    public static ErrorResponse of(boolean success, String message) {
        return new ErrorResponse(success, message);
    }
}