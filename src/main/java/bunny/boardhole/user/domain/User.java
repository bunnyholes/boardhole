package bunny.boardhole.user.domain;

import bunny.boardhole.shared.constants.ValidationConstants;
import bunny.boardhole.shared.util.MessageUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.Assert;

import java.io.*;
import java.time.*;
import java.util.Set;

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

    @Column(nullable = false, unique = true)
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
    private Set<Role> roles;

    // 필요한 필드만 받는 생성자에 @Builder 적용
    @Builder
    public User(String username, String password, String name, String email, java.util.Set<Role> roles) {
        Assert.hasText(username, MessageUtils.get("validation.user.username.required"));
        Assert.isTrue(username.length() <= ValidationConstants.USER_USERNAME_MAX_LENGTH, MessageUtils.get("validation.user.username.too-long", ValidationConstants.USER_USERNAME_MAX_LENGTH));
        Assert.hasText(password, MessageUtils.get("validation.user.password.required"));
        Assert.hasText(name, MessageUtils.get("validation.user.name.required"));
        Assert.isTrue(name.length() <= ValidationConstants.USER_NAME_MAX_LENGTH, MessageUtils.get("validation.user.name.too-long", ValidationConstants.USER_NAME_MAX_LENGTH));
        Assert.hasText(email, MessageUtils.get("validation.user.email.required"));
        Assert.isTrue(email.length() <= ValidationConstants.USER_EMAIL_MAX_LENGTH, MessageUtils.get("validation.user.email.too-long", ValidationConstants.USER_EMAIL_MAX_LENGTH));

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

    public void changeName(String name) {
        Assert.hasText(name, MessageUtils.get("validation.user.name.required"));
        Assert.isTrue(name.length() <= ValidationConstants.USER_NAME_MAX_LENGTH, MessageUtils.get("validation.user.name.too-long", ValidationConstants.USER_NAME_MAX_LENGTH));
        this.name = name;
    }

    public void changeEmail(String email) {
        Assert.hasText(email, MessageUtils.get("validation.user.email.required"));
        Assert.isTrue(email.length() <= ValidationConstants.USER_EMAIL_MAX_LENGTH, MessageUtils.get("validation.user.email.too-long", ValidationConstants.USER_EMAIL_MAX_LENGTH));
        this.email = email;
    }

    public void changePassword(String password) {
        Assert.hasText(password, MessageUtils.get("validation.user.password.required"));
        this.password = password;
    }

    public void recordLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void verifyEmail() {
        emailVerified = true;
        emailVerifiedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

}
