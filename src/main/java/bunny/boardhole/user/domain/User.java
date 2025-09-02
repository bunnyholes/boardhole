package bunny.boardhole.user.domain;

import bunny.boardhole.shared.constants.*;
import bunny.boardhole.shared.util.EntityMessageProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"password", "roles"})
@Entity
@DynamicUpdate
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_name", columnList = "name")
})
@Schema(name = "User", description = "사용자 도메인 엔티티 - 시스템의 핵심 사용자 정보")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Schema(description = "사용자 고유 ID (자동 생성)", example = "1")
    private Long id;

    @Column(nullable = false, unique = true, length = ValidationConstants.USER_USERNAME_MAX_LENGTH)
    @Schema(description = "사용자명 (고유값, 3-20자)", example = "johndoe")
    private String username;

    @Column(nullable = false, length = ValidationConstants.USER_PASSWORD_MAX_LENGTH)
    @Schema(description = "암호화된 비밀번호", example = "$2a$10$encrypted...")
    private String password;

    @Column(nullable = false, length = ValidationConstants.USER_NAME_MAX_LENGTH)
    @Schema(description = "사용자 실명 (1-50자)", example = "홍길동")
    private String name;

    @Column(nullable = false, unique = true, length = ValidationConstants.USER_EMAIL_MAX_LENGTH)
    @Schema(description = "이메일 주소 (고유값)", example = "john@example.com")
    private String email;

    @Column(name = "created_at")
    @Schema(description = "계정 생성 일시 (자동 설정)", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "마지막 수정 일시 (자동 갱신)", example = "2024-01-16T14:20:15")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    @Schema(description = "마지막 로그인 일시", example = "2024-01-16T14:20:15")
    private LocalDateTime lastLogin;

    @Column(name = "email_verified", nullable = false)
    @Schema(description = "이메일 인증 여부", example = "false")
    private boolean emailVerified = false;

    @Column(name = "email_verified_at")
    @Schema(description = "이메일 인증 완료 일시", example = "2024-01-16T14:20:15")
    private LocalDateTime emailVerifiedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Schema(description = "사용자 권한 목록 (USER, ADMIN)", example = "[\"USER\", \"ADMIN\"]")
    private java.util.Set<Role> roles;

    // 필요한 필드만 받는 생성자에 @Builder 적용
    @Builder
    public User(@NonNull String username, @NonNull String password, @NonNull String name, @NonNull String email, java.util.Set<Role> roles) {
        Assert.hasText(username, EntityMessageProvider.getMessage(ValidationMessages.USER_USERNAME_REQUIRED, ValidationMessages.USER_USERNAME_REQUIRED_FALLBACK));
        Assert.isTrue(username.length() <= ValidationConstants.USER_USERNAME_MAX_LENGTH, EntityMessageProvider.getMessage(ValidationMessages.USER_USERNAME_TOO_LONG, ValidationMessages.USER_USERNAME_TOO_LONG_FALLBACK, ValidationConstants.USER_USERNAME_MAX_LENGTH));
        Assert.hasText(password, EntityMessageProvider.getMessage(ValidationMessages.USER_PASSWORD_REQUIRED, ValidationMessages.USER_PASSWORD_REQUIRED_FALLBACK));
        Assert.hasText(name, EntityMessageProvider.getMessage(ValidationMessages.USER_NAME_REQUIRED, ValidationMessages.USER_NAME_REQUIRED_FALLBACK));
        Assert.isTrue(name.length() <= ValidationConstants.USER_NAME_MAX_LENGTH, EntityMessageProvider.getMessage(ValidationMessages.USER_NAME_TOO_LONG, ValidationMessages.USER_NAME_TOO_LONG_FALLBACK, ValidationConstants.USER_NAME_MAX_LENGTH));
        Assert.hasText(email, EntityMessageProvider.getMessage(ValidationMessages.USER_EMAIL_REQUIRED, ValidationMessages.USER_EMAIL_REQUIRED_FALLBACK));
        Assert.isTrue(email.length() <= ValidationConstants.USER_EMAIL_MAX_LENGTH, EntityMessageProvider.getMessage(ValidationMessages.USER_EMAIL_TOO_LONG, ValidationMessages.USER_EMAIL_TOO_LONG_FALLBACK, ValidationConstants.USER_EMAIL_MAX_LENGTH));

        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.roles = roles;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void changeName(@NonNull String name) {
        Assert.hasText(name, EntityMessageProvider.getMessage(ValidationMessages.USER_NAME_REQUIRED, ValidationMessages.USER_NAME_REQUIRED_FALLBACK));
        Assert.isTrue(name.length() <= ValidationConstants.USER_NAME_MAX_LENGTH, EntityMessageProvider.getMessage(ValidationMessages.USER_NAME_TOO_LONG, ValidationMessages.USER_NAME_TOO_LONG_FALLBACK, ValidationConstants.USER_NAME_MAX_LENGTH));
        this.name = name;
    }

    public void changeEmail(@NonNull String email) {
        Assert.hasText(email, EntityMessageProvider.getMessage(ValidationMessages.USER_EMAIL_REQUIRED, ValidationMessages.USER_EMAIL_REQUIRED_FALLBACK));
        Assert.isTrue(email.length() <= ValidationConstants.USER_EMAIL_MAX_LENGTH, EntityMessageProvider.getMessage(ValidationMessages.USER_EMAIL_TOO_LONG, ValidationMessages.USER_EMAIL_TOO_LONG_FALLBACK, ValidationConstants.USER_EMAIL_MAX_LENGTH));
        this.email = email;
    }

    public void changePassword(@NonNull String password) {
        Assert.hasText(password, EntityMessageProvider.getMessage(ValidationMessages.USER_PASSWORD_REQUIRED, ValidationMessages.USER_PASSWORD_REQUIRED_FALLBACK));
        this.password = password;
    }

    public void recordLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void grantAdminRole() {
        if (!this.roles.contains(Role.ADMIN)) {
            this.roles.add(Role.ADMIN);
        }
    }

    public boolean revokeAdminRole() {
        if (this.roles.contains(Role.ADMIN)) {
            if (this.roles.size() <= 1) {
                return false; // 마지막 역할이 ADMIN인 경우 제거 불가
            }
            this.roles.remove(Role.ADMIN);
            return true;
        }
        return false; // ADMIN 역할이 없음
    }

    public boolean hasAdminRole() {
        return this.roles.contains(Role.ADMIN);
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean canAccessService() {
        return emailVerified;
    }
}
