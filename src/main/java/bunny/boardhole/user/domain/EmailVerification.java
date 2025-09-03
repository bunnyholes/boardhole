package bunny.boardhole.user.domain;

import bunny.boardhole.shared.constants.*;
import bunny.boardhole.shared.util.EntityMessageProvider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "email_verifications", indexes = {
        @Index(name = "idx_email_verification_user_id", columnList = "user_id"),
        @Index(name = "idx_email_verification_expires_at", columnList = "expires_at")
})
public class EmailVerification {

    @Id
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "new_email", nullable = false, length = ValidationConstants.USER_EMAIL_MAX_LENGTH)
    private String newEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", nullable = false)
    private EmailVerificationType verificationType;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public EmailVerification(@NonNull String code, @NonNull Long userId, @NonNull String newEmail,
                             @NonNull LocalDateTime expiresAt, @NonNull EmailVerificationType verificationType) {
        Assert.hasText(code, EntityMessageProvider.getMessage(ValidationMessages.EMAIL_VERIFICATION_CODE_REQUIRED, ValidationMessages.EMAIL_VERIFICATION_CODE_REQUIRED_FALLBACK));
        Assert.notNull(userId, EntityMessageProvider.getMessage(ValidationMessages.EMAIL_VERIFICATION_USER_ID_REQUIRED, ValidationMessages.EMAIL_VERIFICATION_USER_ID_REQUIRED_FALLBACK));
        Assert.hasText(newEmail, EntityMessageProvider.getMessage(ValidationMessages.EMAIL_VERIFICATION_NEW_EMAIL_REQUIRED, ValidationMessages.EMAIL_VERIFICATION_NEW_EMAIL_REQUIRED_FALLBACK));
        Assert.notNull(expiresAt, EntityMessageProvider.getMessage(ValidationMessages.EMAIL_VERIFICATION_EXPIRES_AT_REQUIRED, ValidationMessages.EMAIL_VERIFICATION_EXPIRES_AT_REQUIRED_FALLBACK));
        Assert.notNull(verificationType, EntityMessageProvider.getMessage(ValidationMessages.EMAIL_VERIFICATION_TYPE_REQUIRED, ValidationMessages.EMAIL_VERIFICATION_TYPE_REQUIRED_FALLBACK));

        this.code = code;
        this.userId = userId;
        this.newEmail = newEmail;
        this.expiresAt = expiresAt;
        this.verificationType = verificationType;
        this.used = false;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void markAsUsed() {
        Assert.state(!this.used, "이미 사용된 검증 코드입니다");
        Assert.state(LocalDateTime.now(ZoneId.systemDefault()).isBefore(expiresAt), "만료된 검증 코드입니다");
        this.used = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now(ZoneId.systemDefault()).isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}