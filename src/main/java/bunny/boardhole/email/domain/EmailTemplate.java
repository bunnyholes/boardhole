package bunny.boardhole.email.domain;

import lombok.Getter;

/**
 * 이메일 템플릿 유형을 정의하는 열거형
 */
@Getter
public enum EmailTemplate {

    /**
     * 회원가입 이메일 인증 템플릿
     */
    SIGNUP_VERIFICATION("signup-verification", "이메일 인증을 완료해주세요"),

    /**
     * 이메일 주소 변경 인증 템플릿
     */
    EMAIL_CHANGE_VERIFICATION("email-change-verification", "이메일 변경 인증을 완료해주세요"),

    /**
     * 회원가입 환영 메시지 템플릿
     */
    WELCOME("welcome", "Board-Hole에 오신 것을 환영합니다!"),

    /**
     * 이메일 변경 완료 알림 템플릿
     */
    EMAIL_CHANGED("email-changed", "이메일 주소가 성공적으로 변경되었습니다");

    /**
     * 템플릿 이름
     */
    private final String templateName;

    /**
     * 기본 제목
     */
    private final String defaultSubject;

    /**
     * EmailTemplate 생성자
     *
     * @param templateName   템플릿 이름
     * @param defaultSubject 기본 제목
     */
    EmailTemplate(String templateName, String defaultSubject) {
        this.templateName = templateName;
        this.defaultSubject = defaultSubject;
    }

    /**
     * 템플릿 파일 이름을 반환합니다.
     *
     * @return 템플릿 파일 이름
     */
    public String getTemplateFileName() {
        return templateName + ".html";
    }
}