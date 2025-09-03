package bunny.boardhole.user.domain;

import lombok.Getter;

@Getter
public enum EmailVerificationType {

    SIGNUP("회원가입 인증"),

    CHANGE_EMAIL("이메일 변경 인증");

    private final String description;

    EmailVerificationType(String description) {
        this.description = description;
    }
}