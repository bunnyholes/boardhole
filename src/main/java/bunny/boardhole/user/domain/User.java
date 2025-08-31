package bunny.boardhole.user.domain;

import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_name", columnList = "name")
})
@Schema(name = "User", description = "사용자 도메인 엔티티 - 시스템의 핵심 사용자 정보")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    public User(String username, String password, String name, String email, java.util.Set<Role> roles) {
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

    public void changeName(String name) {
        this.name = name;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void recordLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id != null && id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
