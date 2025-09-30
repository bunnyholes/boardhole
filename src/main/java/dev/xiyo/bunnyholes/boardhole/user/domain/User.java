package dev.xiyo.bunnyholes.boardhole.user.domain;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.lang.Nullable;

import dev.xiyo.bunnyholes.boardhole.shared.domain.BaseEntity;
import dev.xiyo.bunnyholes.boardhole.shared.domain.listener.ValidationListener;
import dev.xiyo.bunnyholes.boardhole.user.domain.validation.UserValidationConstants;
import dev.xiyo.bunnyholes.boardhole.user.domain.validation.required.ValidEmail;
import dev.xiyo.bunnyholes.boardhole.user.domain.validation.required.ValidEncodedPassword;
import dev.xiyo.bunnyholes.boardhole.user.domain.validation.required.ValidName;
import dev.xiyo.bunnyholes.boardhole.user.domain.validation.required.ValidUsername;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"password", "roles"})
@Entity
@EntityListeners(ValidationListener.class)
@DynamicUpdate
@Table(name = "users", indexes = {@Index(name = "idx_user_username", columnList = "username"), @Index(name = "idx_user_email", columnList = "email"), @Index(name = "idx_user_name", columnList = "name")})
public class User extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ValidUsername
    @Column(nullable = false, unique = true, length = UserValidationConstants.USER_USERNAME_MAX_LENGTH)
    private String username;

    @ValidEncodedPassword
    @Column(nullable = false, length = UserValidationConstants.PASSWORD_ENCODED_BCRYPT_LENGTH)
    private String password;

    @ValidName
    @Column(nullable = false, length = UserValidationConstants.USER_NAME_MAX_LENGTH)
    private String name;

    @ValidEmail
    @Column(nullable = false, unique = true, length = UserValidationConstants.USER_EMAIL_MAX_LENGTH)
    private String email;

    @Column
    private @Nullable LocalDateTime lastLogin;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column
    private @Nullable LocalDateTime emailVerifiedAt;

    @Getter(AccessLevel.NONE)
    @NotEmpty(message = "{validation.user.roles.empty}")
    @ElementCollection
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column
    private Set<Role> roles;

    // 필요한 필드만 받는 생성자에 @Builder 적용
    @Builder
    private User(String username, String password, String name, String email, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.roles = new HashSet<>(roles);
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

    public void recordLastLogin() {
        lastLogin = LocalDateTime.now();
    }

    public void verifyEmail() {
        emailVerified = true;
        emailVerifiedAt = LocalDateTime.now();
    }

    /**
     * 사용자가 특정 권한을 가지고 있는지 확인합니다.
     *
     * @param role 확인할 권한
     * @return 해당 권한을 가지고 있으면 true
     */
    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    /**
     * 사용자에게 특정 권한을 부여합니다.
     * <p>
     * 기존 권한을 유지하면서 새로운 역할을 추가합니다.
     * 이미 해당 권한이 있는 경우 중복 추가되지 않습니다.
     * </p>
     *
     * @param role 부여할 권한
     */
    public void grantRole(Role role) {
        roles.add(role);  // HashSet이므로 중복은 자동으로 방지됨
    }

    /**
     * 사용자의 특정 권한을 철회합니다.
     * <p>
     * 권한 집합에서 특정 역할을 제거합니다.
     * 해당 권한이 없는 경우 아무 작업도 수행하지 않습니다.
     * </p>
     *
     * @param role 철회할 권한
     */
    public void revokeRole(Role role) {
        roles.remove(role);  // 없으면 아무 일도 일어나지 않음
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

}
