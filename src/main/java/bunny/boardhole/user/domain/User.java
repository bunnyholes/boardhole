package bunny.boardhole.user.domain;

import bunny.boardhole.shared.constants.ValidationConstants;
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
/**
 * 사용자 도메인 엔티티 클래스.
 * 시스템의 핵심 사용자 정보를 관리하는 엔티티입니다.
 * JPA를 통해 데이터베이스와 매핑되며, 사용자 인증, 권한 관리, 프로필 관리 등의
 * 핵심 비즈니스 로직을 캡슐화합니다.
 *
 * @author User Team
 * @version 1.0
 * @since 1.0
 */
@Schema(name = "User", description = "사용자 도메인 엔티티 - 시스템의 핵심 사용자 정보")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** User unique identifier (auto-generated primary key) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Schema(description = "사용자 고유 ID (자동 생성)", example = "1")
    private Long id;

    /** Username for login (unique, 3-20 characters) */
    @Column(nullable = false, unique = true, length = ValidationConstants.USER_USERNAME_MAX_LENGTH)
    @Schema(description = "사용자명 (고유값, 3-20자)", example = "johndoe")
    private String username;

    /** Encrypted password for authentication */
    @Column(nullable = false, length = ValidationConstants.USER_PASSWORD_MAX_LENGTH)
    @Schema(description = "암호화된 비밀번호", example = "$2a$10$encrypted...")
    private String password;

    /** User's display name (1-50 characters) */
    @Column(nullable = false, length = ValidationConstants.USER_NAME_MAX_LENGTH)
    @Schema(description = "사용자 실명 (1-50자)", example = "홍길동")
    private String name;

    /** Email address for contact and verification (unique) */
    @Column(nullable = false, unique = true, length = ValidationConstants.USER_EMAIL_MAX_LENGTH)
    @Schema(description = "이메일 주소 (고유값)", example = "john@example.com")
    private String email;

    /** Account creation timestamp (auto-set) */
    @Column(name = "created_at")
    @Schema(description = "계정 생성 일시 (자동 설정)", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    /** Last modification timestamp (auto-updated) */
    @Column(name = "updated_at")
    @Schema(description = "마지막 수정 일시 (자동 갱신)", example = "2024-01-16T14:20:15")
    private LocalDateTime updatedAt;

    /** Last login timestamp for security tracking */
    @Column(name = "last_login")
    @Schema(description = "마지막 로그인 일시", example = "2024-01-16T14:20:15")
    private LocalDateTime lastLogin;

    /** User roles and permissions (USER, ADMIN, etc.) */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Schema(description = "사용자 권한 목록 (USER, ADMIN)", example = "[\"USER\", \"ADMIN\"]")
    private java.util.Set<Role> roles;

    /**
     * 사용자를 생성하는 Builder 패턴 생성자입니다.
     * <p>
     * 필수 필드인 사용자명, 비밀번호, 이름, 이메일과 선택적 권한 집합을 받아
     * 새로운 사용자 객체를 생성합니다. 생성 시 유효성 검증을 수행하여 도메인 규칙을 강제합니다.
     * </p>
     *
     * @param username  사용자명 (필수, 고유값, 최대 20자)
     * @param password  비밀번호 (필수, 암호화 예정)
     * @param name      이름 (필수, 최대 50자)
     * @param email     이메일 주소 (필수, 고유값)
     * @param userRoles 사용자 권한 집합 (선택, null 가능)
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    @Builder
    public User(@NonNull final String username, @NonNull final String password, @NonNull final String name, @NonNull final String email, final java.util.Set<Role> userRoles) {
        Assert.hasText(username, "사용자명은 필수입니다");
        Assert.isTrue(username.length() <= ValidationConstants.USER_USERNAME_MAX_LENGTH, "사용자명은 " + ValidationConstants.USER_USERNAME_MAX_LENGTH + "자를 초과할 수 없습니다");
        Assert.hasText(password, "비밀번호는 필수입니다");
        Assert.hasText(name, "이름은 필수입니다");
        Assert.isTrue(name.length() <= ValidationConstants.USER_NAME_MAX_LENGTH, "이름은 " + ValidationConstants.USER_NAME_MAX_LENGTH + "자를 초과할 수 없습니다");
        Assert.hasText(email, "이메일은 필수입니다");
        Assert.isTrue(email.length() <= ValidationConstants.USER_EMAIL_MAX_LENGTH, "이메일은 " + ValidationConstants.USER_EMAIL_MAX_LENGTH + "자를 초과할 수 없습니다");

        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.roles = userRoles;
    }

    /**
     * JPA 엔티티 영속화 전 실행되는 콜백 메소드입니다.
     * <p>
     * 생성일시와 수정일시를 현재 시각으로 초기화합니다.
     * 데이터베이스에 처음 저장될 때 한 번만 실행됩니다.
     * </p>
     */
    @PrePersist
    public void prePersist() {
        final LocalDateTime currentTime = LocalDateTime.now();
        if (createdAt == null) createdAt = currentTime;
        if (updatedAt == null) updatedAt = currentTime;
    }

    /**
     * JPA 엔티티 업데이트 전 실행되는 콜백 메소드입니다.
     * <p>
     * 수정일시를 현재 시각으로 자동 갱신합니다.
     * 엔티티가 변경될 때마다 자동으로 실행됩니다.
     * </p>
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자의 이름을 변경합니다.
     * <p>
     * 이름 변경 시 유효성 검증을 수행하여 빈 문자열이나 최대 길이를 초과하는
     * 이름은 허용하지 않습니다.
     * </p>
     *
     * @param newName 새로운 이름 (필수, 최대 50자)
     * @throws IllegalArgumentException 이름이 유효하지 않은 경우
     */
    public void changeName(@NonNull final String newName) {
        Assert.hasText(newName, "이름은 필수입니다");
        Assert.isTrue(newName.length() <= ValidationConstants.USER_NAME_MAX_LENGTH, "이름은 " + ValidationConstants.USER_NAME_MAX_LENGTH + "자를 초과할 수 없습니다");
        this.name = newName;
    }

    /**
     * 사용자의 이메일 주소를 변경합니다.
     * <p>
     * 이메일 변경 시 유효성 검증을 수행하여 빈 문자열이나 최대 길이를 초과하는
     * 이메일은 허용하지 않습니다.
     * </p>
     *
     * @param newEmail 새로운 이메일 주소 (필수, 최대 100자)
     * @throws IllegalArgumentException 이메일이 유효하지 않은 경우
     */
    public void changeEmail(@NonNull final String newEmail) {
        Assert.hasText(newEmail, "이메일은 필수입니다");
        Assert.isTrue(newEmail.length() <= ValidationConstants.USER_EMAIL_MAX_LENGTH, "이메일은 " + ValidationConstants.USER_EMAIL_MAX_LENGTH + "자를 초과할 수 없습니다");
        this.email = newEmail;
    }

    /**
     * 사용자의 비밀번호를 변경합니다.
     * <p>
     * 비밀번호 변경 시 유효성 검증을 수행하여 빈 문자열은 허용하지 않습니다.
     * 이 메소드는 암호화된 비밀번호를 기대하므로 호출 전에 암호화가 되어야 합니다.
     * </p>
     *
     * @param newPassword 새로운 비밀번호 (필수, 암호화됨)
     * @throws IllegalArgumentException 비밀번호가 빈 문자열인 경우
     */
    public void changePassword(@NonNull final String newPassword) {
        Assert.hasText(newPassword, "비밀번호는 필수입니다");
        this.password = newPassword;
    }

    /**
     * 사용자의 마지막 로그인 시각을 기록합니다.
     * <p>
     * 사용자가 성공적으로 로그인할 때마다 호출되어 로그인 이력을 추적합니다.
     * 보안 목적으로 비정상적인 로그인 패턴을 감지하는 데 활용될 수 있습니다.
     * </p>
     *
     * @param loginTimestamp 로그인 시각 (선택, null 가능)
     */
    public void recordLastLogin(final LocalDateTime loginTimestamp) {
        this.lastLogin = loginTimestamp;
    }
}
