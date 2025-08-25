package bunny.boardhole.dto.board;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BoardRequest {
    @NotBlank(groups = Create.class)
    private String title;
    
    @NotBlank(groups = Create.class)
    private String content;
    
    // Validation groups
    public interface Create {}
    public interface Update {}
}