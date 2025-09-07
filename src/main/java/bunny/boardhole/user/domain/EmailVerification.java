package bunny.boardhole.user.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.DynamicUpdate;

import bunny.boardhole.shared.domain.BaseEntity;
import bunny.boardhole.shared.domain.listener.ValidationListener;
import bunny.boardhole.user.domain.validation.required.ValidNewEmail;
import bunny.boardhole.user.domain.validation.required.ValidVerificationCode;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = "user")
@Entity
@EntityListeners(ValidationListener.class)
@DynamicUpdate
@Table(name = "email_verifications", indexes = {@Index(name = "idx_email_verification_user_id", columnList = "user_id"), @Index(name = "idx_email_verification_expires_at", columnList = "expires_at"), @Index(name = "idx_email_verification_type_used", columnList = "verification_type, used")})
public class EmailVerification extends BaseEntity {

    @Id
    @ValidVerificationCode
    @EqualsAndHashCode.Include
    private String code;

    @NotNull(message = "{validation.email.verification.user.required}")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ValidNewEmail
    @Column(nullable = false)
    private String newEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailVerificationType verificationType;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Builder
    public EmailVerification(String code, User user, String newEmail, LocalDateTime expiresAt, EmailVerificationType verificationType) {
        this.code = code;
        this.user = user;
        this.newEmail = newEmail;
        this.expiresAt = expiresAt;
        this.verificationType = verificationType;
    }

    public void markAsUsed() {
        if (used)
            throw new IllegalStateException("Email verification code has already been used");
        if (isExpired())
            throw new IllegalStateException("Email verification code has expired");
        used = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now(ZoneId.systemDefault()).isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}
