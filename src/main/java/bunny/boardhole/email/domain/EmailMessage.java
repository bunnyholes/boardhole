package bunny.boardhole.email.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.*;

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
@Schema(name = "EmailMessage", description = "이메일 메시지 도메인 객체")
public record EmailMessage(
        @NonNull @Schema(description = "받는 사람 이메일 주소", example = "user@example.com") String recipientEmail,
        @NonNull @Schema(description = "이메일 제목", example = "이메일 인증 안내") String subject,
        @NonNull @Schema(description = "이메일 내용 (HTML)", example = "<h1>환영합니다!</h1>") String content,
        @Schema(description = "참조 수신자 목록") List<String> carbonCopy,
        @Schema(description = "숨은 참조 수신자 목록") List<String> blindCarbonCopy,
        @Schema(description = "이메일 템플릿 변수들") Map<String, Object> templateVariables) {

    /**
     * 기본 이메일 메시지를 생성합니다.
     *
     * @param recipientEmail 받는 사람 이메일 주소
     * @param emailSubject   이메일 제목
     * @param emailContent   이메일 내용
     * @return 생성된 이메일 메시지
     */
    public static EmailMessage create(@NonNull final String recipientEmail, @NonNull final String emailSubject, @NonNull final String emailContent) {
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
    public static EmailMessage createFromTemplate(@NonNull final String recipientEmail, @NonNull final String emailSubject,
                                                  @NonNull final String emailTemplate, @NonNull final Map<String, Object> templateVariables) {
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