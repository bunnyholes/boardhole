package bunny.boardhole.email.infrastructure;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import bunny.boardhole.email.application.EmailTemplateService;
import bunny.boardhole.email.domain.EmailTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Thymeleaf 기반 이메일 템플릿 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThymeleafEmailTemplateService implements EmailTemplateService {

    private final TemplateEngine templateEngine;

    @Override
    public String processTemplate(final EmailTemplate emailTemplate, final Map<String, Object> templateVariables) {
        try {
            final Context context = new Context();
            templateVariables.forEach(context::setVariable);

            final String templatePath = getTemplatePath(emailTemplate);
            final String processedContent = templateEngine.process(templatePath, context);

            log.debug("템플릿 처리 완료: template={}, variables={}", emailTemplate.getTemplateName(), templateVariables.keySet());
            return processedContent;

        } catch (final Exception e) {
            log.error("템플릿 처리 실패: template={}, error={}", emailTemplate.getTemplateName(), e.getMessage(), e);
            throw new RuntimeException("이메일 템플릿 처리에 실패했습니다", e);
        }
    }

    @Override
    public String getTemplatePath(final EmailTemplate emailTemplate) {
        return "email/" + emailTemplate.getTemplateFileName();
    }
}