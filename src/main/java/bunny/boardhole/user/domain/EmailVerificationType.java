package bunny.boardhole.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EmailVerificationType", description = "이메일 인증 타입")
public enum EmailVerificationType {
    
    @Schema(description = "회원가입 시 이메일 인증")
    SIGNUP("회원가입 인증"),
    
    @Schema(description = "이메일 주소 변경 인증")
    CHANGE_EMAIL("이메일 변경 인증");

    private final String description;

    EmailVerificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}