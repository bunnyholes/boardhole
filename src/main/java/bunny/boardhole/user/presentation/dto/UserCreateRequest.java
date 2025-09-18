package bunny.boardhole.user.presentation.dto;

import bunny.boardhole.shared.validation.PasswordMatch;
import bunny.boardhole.user.domain.validation.UserValidationConstants;
import bunny.boardhole.user.domain.validation.required.ValidEmail;
import bunny.boardhole.user.domain.validation.required.ValidName;
import bunny.boardhole.user.domain.validation.required.ValidPassword;
import bunny.boardhole.user.domain.validation.required.ValidUsername;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@PasswordMatch
@Schema(name = "UserCreateRequest", description = "사용자 생성 요청")
public record UserCreateRequest(
        @ValidUsername @Schema(description = "사용자명 (3-20자, 영문/숫자)", example = "user01", minLength = UserValidationConstants.USER_USERNAME_MIN_LENGTH, maxLength = UserValidationConstants.USER_USERNAME_MAX_LENGTH, requiredMode = Schema.RequiredMode.REQUIRED) String username,

        @ValidPassword @Schema(description = "비밀번호 (8-100자)", example = "User1234!", minLength = UserValidationConstants.USER_PASSWORD_MIN_LENGTH, maxLength = UserValidationConstants.USER_PASSWORD_MAX_LENGTH, requiredMode = Schema.RequiredMode.REQUIRED) String password,

        @NotBlank(message = "{validation.user.password.confirm.required}") @Schema(description = "비밀번호 확인", example = "User1234!", requiredMode = Schema.RequiredMode.REQUIRED) String confirmPassword,

        @ValidName @Schema(description = "이름 (1-50자)", example = "홍길동", minLength = UserValidationConstants.USER_NAME_MIN_LENGTH, maxLength = UserValidationConstants.USER_NAME_MAX_LENGTH, requiredMode = Schema.RequiredMode.REQUIRED) String name,

        @ValidEmail @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED) String email) {
}
