package bunny.boardhole.email.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Schema(name = "EmailMessage", description = "이메일 메시지 도메인 객체")
public class EmailMessage {

    @NonNull
    @Schema(description = "받는 사람 이메일 주소", example = "user@example.com")
    private final String to;

    @NonNull
    @Schema(description = "이메일 제목", example = "이메일 인증 안내")
    private final String subject;

    @NonNull
    @Schema(description = "이메일 내용 (HTML)", example = "<h1>환영합니다!</h1>")
    private final String content;

    @Schema(description = "참조 수신자 목록")
    private final List<String> cc;

    @Schema(description = "숨은 참조 수신자 목록")
    private final List<String> bcc;

    @Schema(description = "이메일 템플릿 변수들")
    private final Map<String, Object> templateVariables;

    public static EmailMessage create(@NonNull String to, @NonNull String subject, @NonNull String content) {
        Assert.hasText(to, "받는 사람 이메일은 필수입니다");
        Assert.hasText(subject, "이메일 제목은 필수입니다");
        Assert.hasText(content, "이메일 내용은 필수입니다");
        
        return EmailMessage.builder()
                .to(to)
                .subject(subject)
                .content(content)
                .build();
    }

    public static EmailMessage createFromTemplate(@NonNull String to, @NonNull String subject, 
                                                @NonNull String template, @NonNull Map<String, Object> variables) {
        Assert.hasText(to, "받는 사람 이메일은 필수입니다");
        Assert.hasText(subject, "이메일 제목은 필수입니다");
        Assert.hasText(template, "템플릿은 필수입니다");
        Assert.notNull(variables, "템플릿 변수는 필수입니다");
        
        return EmailMessage.builder()
                .to(to)
                .subject(subject)
                .content(template)
                .templateVariables(variables)
                .build();
    }
}