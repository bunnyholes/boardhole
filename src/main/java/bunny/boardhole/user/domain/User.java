package bunny.boardhole.user.domain;

import bunny.boardhole.shared.constants.*;
import bunny.boardhole.shared.util.EntityMessageProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.*;
import java.time.*;

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
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = -8110205350586224981L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = ValidationConstants.USER_USERNAME_MAX_LENGTH)
    private String username;

    @Column(nullable = false, length = ValidationConstants.USER_PASSWORD_MAX_LENGTH)
    private String password;

    @Column(nullable = false, length = ValidationConstants.USER_NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false, unique = true, length = ValidationConstants.USER_EMAIL_MAX_LENGTH)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
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
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.systemDefault());
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
        roles.add(Role.ADMIN);
    }

    public boolean revokeAdminRole() {
        if (roles.contains(Role.ADMIN)) {
            if (roles.size() <= 1) return false; // 마지막 역할이 ADMIN인 경우 제거 불가
            roles.remove(Role.ADMIN);
            return true;
        }
        return false; // ADMIN 역할이 없음
    }

    public boolean hasAdminRole() {
        return roles.contains(Role.ADMIN);
    }

    public void verifyEmail() {
        emailVerified = true;
        emailVerifiedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    public boolean canAccessService() {
        return emailVerified;
    }
}
