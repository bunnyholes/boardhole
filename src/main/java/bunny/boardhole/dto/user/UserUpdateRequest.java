package bunny.boardhole.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Size(min = 1, max = 50)
    private String name;

    @Email
    private String email;

    @Size(min = 4, max = 100)
    private String password;
}

