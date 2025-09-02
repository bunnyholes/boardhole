package bunny.boardhole.user.domain;

import bunny.boardhole.shared.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "email_verifications", indexes = {
        @Index(name = "idx_email_verification_user_id", columnList = "user_id"),
        @Index(name = "idx_email_verification_expires_at", columnList = "expires_at")
})
@Schema(name = "EmailVerification", description = "이메일 변경 검증 도메인 엔티티")
public class EmailVerification {

    @Id
    @EqualsAndHashCode.Include
    @Schema(description = "검증 코드 (고유값)", example = "ABC123")
    private String code;

    @Column(name = "user_id", nullable = false)
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Column(name = "new_email", nullable = false, length = ValidationConstants.USER_EMAIL_MAX_LENGTH)
    @Schema(description = "변경할 새 이메일 주소", example = "newemail@example.com")
    private String newEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", nullable = false)
    @Schema(description = "인증 타입 (SIGNUP, CHANGE_EMAIL)", example = "SIGNUP")
    private EmailVerificationType verificationType;

    @Column(name = "expires_at", nullable = false)
    @Schema(description = "만료 시간", example = "2024-01-15T10:30:00")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Schema(description = "사용 여부", example = "false")
    private boolean used;

    @Column(name = "created_at")
    @Schema(description = "생성 일시", example = "2024-01-15T10:00:00")
    private LocalDateTime createdAt;

    @Builder
    public EmailVerification(@NonNull String code, @NonNull Long userId, @NonNull String newEmail, 
                           @NonNull LocalDateTime expiresAt, @NonNull EmailVerificationType verificationType) {
        Assert.hasText(code, "검증 코드는 필수입니다");
        Assert.notNull(userId, "사용자 ID는 필수입니다");
        Assert.hasText(newEmail, "새 이메일은 필수입니다");
        Assert.notNull(expiresAt, "만료 시간은 필수입니다");
        Assert.notNull(verificationType, "인증 타입은 필수입니다");

        this.code = code;
        this.userId = userId;
        this.newEmail = newEmail;
        this.expiresAt = expiresAt;
        this.verificationType = verificationType;
        this.used = false;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public void markAsUsed() {
        Assert.state(!this.used, "이미 사용된 검증 코드입니다");
        Assert.state(LocalDateTime.now().isBefore(expiresAt), "만료된 검증 코드입니다");
        this.used = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}