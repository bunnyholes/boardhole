package bunny.boardhole.user.domain;


public enum EmailVerificationType {

    SIGNUP("회원가입 인증"),

    CHANGE_EMAIL("이메일 변경 인증");

    private final String description;

    EmailVerificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}