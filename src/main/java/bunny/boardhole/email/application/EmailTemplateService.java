package bunny.boardhole.email.application;

import java.util.Map;

import bunny.boardhole.email.domain.EmailTemplate;

/**
 * 이메일 템플릿 처리 서비스 인터페이스
 */
public interface EmailTemplateService {

    /**
     * 템플릿을 처리하여 HTML 내용 생성
     *
     * @param emailTemplate     이메일 템플릿
     * @param templateVariables 템플릿 변수들
     * @return 처리된 HTML 내용
     */
    String processTemplate(EmailTemplate emailTemplate, Map<String, Object> templateVariables);

    /**
     * 템플릿 파일 경로 생성
     *
     * @param emailTemplate 이메일 템플릿
     * @return 템플릿 파일 경로
     */
    String getTemplatePath(EmailTemplate emailTemplate);
}