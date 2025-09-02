package bunny.boardhole.email.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EmailTemplate", description = "이메일 템플릿 유형")
public enum EmailTemplate {
    
    @Schema(description = "회원가입 이메일 인증")
    SIGNUP_VERIFICATION("signup-verification", "이메일 인증을 완료해주세요"),
    
    @Schema(description = "이메일 주소 변경 인증")
    EMAIL_CHANGE_VERIFICATION("email-change-verification", "이메일 변경 인증을 완료해주세요"),
    
    @Schema(description = "회원가입 환영 메시지")
    WELCOME("welcome", "Board-Hole에 오신 것을 환영합니다!"),
    
    @Schema(description = "이메일 변경 완료 알림")
    EMAIL_CHANGED("email-changed", "이메일 주소가 성공적으로 변경되었습니다");

    private final String templateName;
    private final String defaultSubject;

    EmailTemplate(String templateName, String defaultSubject) {
        this.templateName = templateName;
        this.defaultSubject = defaultSubject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getDefaultSubject() {
        return defaultSubject;
    }

    public String getTemplateFileName() {
        return templateName + ".html";
    }
}