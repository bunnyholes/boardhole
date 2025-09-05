package bunny.boardhole.user.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import bunny.boardhole.shared.constants.*;
import bunny.boardhole.shared.util.MessageUtils;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

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
        Assert.hasText(username, MessageUtils.get("validation.user.username.required"));
        Assert.isTrue(username.length() <= ValidationConstants.USER_USERNAME_MAX_LENGTH,
                MessageUtils.get("validation.user.username.too-long", ValidationConstants.USER_USERNAME_MAX_LENGTH));
        Assert.hasText(password, MessageUtils.get("validation.user.password.required"));
        Assert.hasText(name, MessageUtils.get("validation.user.name.required"));
        Assert.isTrue(name.length() <= ValidationConstants.USER_NAME_MAX_LENGTH,
                MessageUtils.get("validation.user.name.too-long", ValidationConstants.USER_NAME_MAX_LENGTH));
        Assert.hasText(email, MessageUtils.get("validation.user.email.required"));
        Assert.isTrue(email.length() <= ValidationConstants.USER_EMAIL_MAX_LENGTH,
                MessageUtils.get("validation.user.email.too-long", ValidationConstants.USER_EMAIL_MAX_LENGTH));

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
        Assert.hasText(name, MessageUtils.get("validation.user.name.required"));
        Assert.isTrue(name.length() <= ValidationConstants.USER_NAME_MAX_LENGTH,
                MessageUtils.get("validation.user.name.too-long", ValidationConstants.USER_NAME_MAX_LENGTH));
        this.name = name;
    }

    public void changeEmail(@NonNull String email) {
        Assert.hasText(email, MessageUtils.get("validation.user.email.required"));
        Assert.isTrue(email.length() <= ValidationConstants.USER_EMAIL_MAX_LENGTH,
                MessageUtils.get("validation.user.email.too-long", ValidationConstants.USER_EMAIL_MAX_LENGTH));
        this.email = email;
    }

    public void changePassword(@NonNull String password) {
        Assert.hasText(password, MessageUtils.get("validation.user.password.required"));
        this.password = password;
    }

    public void recordLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    public boolean canAccessService() {
        return emailVerified;
    }

    /**
     * 사용자가 관리자 권한을 가지고 있는지 확인합니다.
     * <p>
     * ADMIN 역할이 사용자의 권한 집합에 포함되어 있는지 검증하여
     * 관리자 권한 여부를 판단합니다.
     * </p>
     *
     * @return 관리자 권한 보유 여부
     */
    public boolean hasAdminRole() {
        return roles != null && roles.contains(Role.ADMIN);
    }

    /**
     * 사용자에게 관리자 권한을 부여합니다.
     * <p>
     * 기존 권한을 유지하면서 ADMIN 역할을 추가합니다.
     * 이미 관리자 권한이 있는 경우 중복 추가되지 않습니다.
     * </p>
     */
    public void grantAdminRole() {
        if (roles == null) {
            roles = new java.util.HashSet<>();
        }
        roles.add(Role.ADMIN);
    }

    /**
     * 사용자의 관리자 권한을 철회합니다.
     * <p>
     * 권한 집합에서 ADMIN 역할을 제거합니다.
     * 관리자 권한이 없는 경우 아무 작업도 수행하지 않습니다.
     * </p>
     *
     * @return 권한 철회 성공 여부
     */
    public boolean revokeAdminRole() {
        if (roles != null && roles.contains(Role.ADMIN)) {
            roles.remove(Role.ADMIN);
            return true;
        }
        return false;
    }

    /**
     * 사용자의 이메일 인증 상태를 확인합니다.
     * <p>
     * 현재 구현에서는 기본적으로 false를 반환하며,
     * 이메일 인증 시스템 구현 시 확장할 예정입니다.
     * </p>
     *
     * @return 이메일 인증 상태 (현재는 항상 false)
     */
    public boolean isEmailVerified() { return emailVerified; }

    
}
