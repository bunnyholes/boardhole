package bunny.boardhole.email.infrastructure;

import bunny.boardhole.email.application.EmailTemplateService;
import bunny.boardhole.email.domain.EmailTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Thymeleaf 기반 이메일 템플릿 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThymeleafEmailTemplateService implements EmailTemplateService {

    private final TemplateEngine templateEngine;

    @Override
    public String processTemplate(EmailTemplate template, Map<String, Object> variables) {
        try {
            Context context = new Context();
            variables.forEach(context::setVariable);
            
            String templatePath = getTemplatePath(template);
            String processedContent = templateEngine.process(templatePath, context);
            
            log.debug("템플릿 처리 완료: template={}, variables={}", template.getTemplateName(), variables.keySet());
            return processedContent;
            
        } catch (Exception e) {
            log.error("템플릿 처리 실패: template={}, error={}", template.getTemplateName(), e.getMessage(), e);
            throw new RuntimeException("이메일 템플릿 처리에 실패했습니다", e);
        }
    }

    @Override
    public String getTemplatePath(EmailTemplate template) {
        return "email/" + template.getTemplateFileName();
    }
}