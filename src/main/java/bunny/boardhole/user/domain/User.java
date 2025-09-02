package bunny.boardhole.user.domain;

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

    @Column(nullable = false, unique = true, length = 20)
    @Schema(description = "사용자명 (고유값, 3-20자)", example = "johndoe")
    private String username;

    @Column(nullable = false, length = 100)
    @Schema(description = "암호화된 비밀번호", example = "$2a$10$encrypted...")
    private String password;

    @Column(nullable = false, length = 50)
    @Schema(description = "사용자 실명 (1-50자)", example = "홍길동")
    private String name;

    @Column(nullable = false, unique = true, length = 255)
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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Schema(description = "사용자 권한 목록 (USER, ADMIN)", example = "[\"USER\", \"ADMIN\"]")
    private java.util.Set<Role> roles;

    // 필요한 필드만 받는 생성자에 @Builder 적용
    @Builder
    public User(@NonNull String username, @NonNull String password, @NonNull String name, @NonNull String email, java.util.Set<Role> roles) {
        Assert.hasText(username, "사용자명은 필수입니다");
        Assert.isTrue(username.length() <= 20, "사용자명은 20자를 초과할 수 없습니다");
        Assert.hasText(password, "비밀번호는 필수입니다");
        Assert.hasText(name, "이름은 필수입니다");
        Assert.isTrue(name.length() <= 50, "이름은 50자를 초과할 수 없습니다");
        Assert.hasText(email, "이메일은 필수입니다");
        Assert.isTrue(email.length() <= 255, "이메일은 255자를 초과할 수 없습니다");

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
        Assert.hasText(name, "이름은 필수입니다");
        Assert.isTrue(name.length() <= 50, "이름은 50자를 초과할 수 없습니다");
        this.name = name;
    }

    public void changeEmail(@NonNull String email) {
        Assert.hasText(email, "이메일은 필수입니다");
        Assert.isTrue(email.length() <= 255, "이메일은 255자를 초과할 수 없습니다");
        this.email = email;
    }

    public void changePassword(@NonNull String password) {
        Assert.hasText(password, "비밀번호는 필수입니다");
        this.password = password;
    }

    public void recordLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
