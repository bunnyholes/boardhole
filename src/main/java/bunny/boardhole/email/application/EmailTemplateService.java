package bunny.boardhole.email.application;

import bunny.boardhole.email.domain.EmailTemplate;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * 이메일 템플릿 처리 서비스 인터페이스
 */
public interface EmailTemplateService {

    /**
     * 템플릿을 처리하여 HTML 내용 생성
     *
     * @param template  이메일 템플릿
     * @param variables 템플릿 변수들
     * @return 처리된 HTML 내용
     */
    String processTemplate(@NonNull EmailTemplate template, @NonNull Map<String, Object> variables);

    /**
     * 템플릿 파일 경로 생성
     *
     * @param template 이메일 템플릿
     * @return 템플릿 파일 경로
     */
    String getTemplatePath(@NonNull EmailTemplate template);
}