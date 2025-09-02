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
/**
 * 이메일 변경 검증 도메인 엔티티 클래스.
 * 사용자의 이메일 주소 변경 요청 시 발송되는 검증 코드를 관리하는 엔티티입니다.
 * 보안상 중요한 역할을 하며, 인증 절차와 만료 대기 처리를 통해
 * 시스템의 무결성을 보장합니다.
 *
 * @author Email Team
 * @version 1.0
 * @since 1.0
 */
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

    @Column(name = "expires_at", nullable = false)
    @Schema(description = "만료 시간", example = "2024-01-15T10:30:00")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Schema(description = "사용 여부", example = "false")
    private boolean used;

    @Column(name = "created_at")
    @Schema(description = "생성 일시", example = "2024-01-15T10:00:00")
    private LocalDateTime createdAt;

    /**
     * 이메일 검증 객체를 생성하는 Builder 패턴 생성자입니다.
     * <p>
     * 이메일 변경 요청 시 새로운 검증 코드를 생성할 때 사용됩니다.
     * 모든 필수 파라미터에 대한 유효성 검증을 수행합니다.
     * </p>
     *
     * @param code      검증 코드 (필수, 고유값)
     * @param userId    사용자 ID (필수)
     * @param newEmail  변경할 새로운 이메일 주소 (필수)
     * @param expiresAt 만료 시각 (필수)
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    @Builder
    public EmailVerification(@NonNull final String code, @NonNull final Long userId, @NonNull final String newEmail, @NonNull final LocalDateTime expiresAt) {
        Assert.hasText(code, "검증 코드는 필수입니다");
        Assert.notNull(userId, "사용자 ID는 필수입니다");
        Assert.hasText(newEmail, "새 이메일은 필수입니다");
        Assert.notNull(expiresAt, "만료 시간은 필수입니다");

        this.code = code;
        this.userId = userId;
        this.newEmail = newEmail;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    /**
     * JPA 엔티티 영속화 전 실행되는 콜백 메소드입니다.
     * <p>
     * 생성일시를 현재 시각으로 초기화합니다.
     * 데이터베이스에 처음 저장되기 전에 한 번만 실행됩니다.
     * </p>
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    /**
     * 검증 코드를 사용 완료 상태로 표시합니다.
     * <p>
     * 이메일 변경 검증이 성공적으로 완료된 후 호출되어 코드의 사용을 마킹합니다.
     * 이미 사용된 코드이거나 만료된 코드인 경우 예외를 발생시킵니다.
     * </p>
     *
     * @throws IllegalStateException 이미 사용되었거나 만료된 경우
     */
    public void markAsUsed() {
        Assert.state(!this.used, "이미 사용된 검증 코드입니다");
        Assert.state(LocalDateTime.now().isBefore(expiresAt), "만료된 검증 코드입니다");
        this.used = true;
    }

    /**
     * 검증 코드가 만료되었는지 확인합니다.
     * <p>
     * 현재 시각과 만료 시각을 비교하여 만료 여부를 판단합니다.
     * 만료된 코드는 사용할 수 없습니다.
     * </p>
     *
     * @return 만료 여부 (true: 만료됨, false: 사용 가능)
     */
    public boolean isExpired() {
        final LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.isAfter(expiresAt);
    }

    /**
     * 검증 코드의 사용 가능 상태를 확인합니다.
     * 코드가 아직 사용되지 않았고 만료되지 않았을 때만 유효합니다.
     *
     * @return 유효성 여부 (true: 사용 가능, false: 사용 불가)
     */
    public boolean isValid() {
        return !used && !isExpired();
    }
}