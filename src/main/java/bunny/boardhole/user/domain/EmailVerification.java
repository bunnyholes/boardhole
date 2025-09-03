package bunny.boardhole.user.domain;

import bunny.boardhole.shared.util.MessageUtils;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

import java.time.*;

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

    @Column(name = "new_email", nullable = false)
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
    public EmailVerification(String code, Long userId, String newEmail,
                             LocalDateTime expiresAt, EmailVerificationType verificationType) {
        Assert.hasText(code, MessageUtils.get("validation.email-verification.code.required"));
        Assert.notNull(userId, MessageUtils.get("validation.email-verification.user-id.required"));
        Assert.hasText(newEmail, MessageUtils.get("validation.email-verification.new-email.required"));
        Assert.notNull(expiresAt, MessageUtils.get("validation.email-verification.expires-at.required"));
        Assert.notNull(verificationType, MessageUtils.get("validation.email-verification.type.required"));

        this.code = code;
        this.userId = userId;
        this.newEmail = newEmail;
        this.expiresAt = expiresAt;
        this.verificationType = verificationType;
        used = false;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void markAsUsed() {
        Assert.state(!used, "이미 사용된 검증 코드입니다");
        Assert.state(LocalDateTime.now(ZoneId.systemDefault()).isBefore(expiresAt), "만료된 검증 코드입니다");
        used = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now(ZoneId.systemDefault()).isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}