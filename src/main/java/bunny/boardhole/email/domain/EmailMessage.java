package bunny.boardhole.email.domain;

import java.util.*;

import org.springframework.util.Assert;

import lombok.Builder;

/**
 * 이메일 메시지를 표현하는 도메인 객체
 *
 * @param recipientEmail    받는 사람 이메일 주소
 * @param subject           이메일 제목
 * @param content           이메일 내용 (HTML)
 * @param carbonCopy        참조 수신자 목록
 * @param blindCarbonCopy   숨은 참조 수신자 목록
 * @param templateVariables 이메일 템플릿 변수들
 */
@Builder
public record EmailMessage(
        String recipientEmail,
        String subject,
        String content,
        List<String> carbonCopy,
        List<String> blindCarbonCopy,
        Map<String, Object> templateVariables) {

    /**
     * 기본 이메일 메시지를 생성합니다.
     *
     * @param recipientEmail 받는 사람 이메일 주소
     * @param emailSubject   이메일 제목
     * @param emailContent   이메일 내용
     * @return 생성된 이메일 메시지
     */
    public static EmailMessage create(final String recipientEmail, final String emailSubject, final String emailContent) {
        Assert.hasText(recipientEmail, "받는 사람 이메일은 필수입니다");
        Assert.hasText(emailSubject, "이메일 제목은 필수입니다");
        Assert.hasText(emailContent, "이메일 내용은 필수입니다");

        return EmailMessage.builder()
                .recipientEmail(recipientEmail)
                .subject(emailSubject)
                .content(emailContent)
                .build();
    }

    /**
     * 템플릿 기반 이메일 메시지를 생성합니다.
     *
     * @param recipientEmail    받는 사람 이메일 주소
     * @param emailSubject      이메일 제목
     * @param emailTemplate     이메일 템플릿
     * @param templateVariables 템플릿 변수들
     * @return 생성된 이메일 메시지
     */
    public static EmailMessage createFromTemplate(final String recipientEmail, final String emailSubject,
                                                  final String emailTemplate, final Map<String, Object> templateVariables) {
        Assert.hasText(recipientEmail, "받는 사람 이메일은 필수입니다");
        Assert.hasText(emailSubject, "이메일 제목은 필수입니다");
        Assert.hasText(emailTemplate, "템플릿은 필수입니다");
        Assert.notNull(templateVariables, "템플릿 변수는 필수입니다");

        return EmailMessage.builder()
                .recipientEmail(recipientEmail)
                .subject(emailSubject)
                .content(emailTemplate)
                .templateVariables(templateVariables)
                .build();
    }
}