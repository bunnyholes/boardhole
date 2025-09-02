package bunny.boardhole.shared.security;

import bunny.boardhole.user.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 애플리케이션 사용자 인증 주체 정보를 나타내는 Spring Security UserDetails 구현체입니다.
 * 
 * <p>Spring Security에서 인증된 사용자의 상세 정보를 제공하며,
 * 사용자의 권한, 계정 상태, 자격 증명 정보 등을 캡슐화합니다.</p>
 * 
 * <p><strong>보안 특징:</strong></p>
 * <ul>
 *   <li><strong>권한 관리:</strong> 사용자의 역할(Role)을 Spring Security의 GrantedAuthority로 변환</li>
 *   <li><strong>계정 상태:</strong> 계정 만료, 잠금, 자격증명 만료 상태 관리</li>
 *   <li><strong>불변 객체:</strong> record 타입으로 구현되어 상태 변경 불가</li>
 * </ul>
 * 
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // SecurityContext에서 사용자 정보 획득
 * Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 * if (auth.getPrincipal() instanceof AppUserPrincipal(User user)) {
 *     // 사용자 정보 활용
 *     String username = user.getUsername();
 *     Set<Role> roles = user.getRoles();
 * }
 * }</pre>
 * 
 * @param user 인증된 사용자의 도메인 객체 (null 불가)
 * 
 * @author Security Team
 * @version 1.0
 * @since 1.0
 * 
 * @see UserDetails
 * @see org.springframework.security.core.Authentication
 * @see bunny.boardhole.user.domain.User
 */
@Schema(name = "AppUserPrincipal", description = "인증된 사용자 주체 정보 - Spring Security UserDetails 구현체")
public record AppUserPrincipal(
        @Schema(description = "인증된 사용자 도메인 객체")
        User user
) implements UserDetails {
    /**
     * 사용자의 권한 목록을 Spring Security의 GrantedAuthority 형태로 반환합니다.
     * 
     * <p>사용자의 역할(Role)을 "ROLE_" 접두사와 함께 SimpleGrantedAuthority로 변환하여
     * Spring Security의 권한 체계에서 사용할 수 있도록 합니다.</p>
     * 
     * <p><strong>보안 고려사항:</strong></p>
     * <ul>
     *   <li>null 역할은 자동으로 필터링되어 보안 예외를 방지</li>
     *   <li>"ROLE_" 접두사를 통해 Spring Security 권한 명명 규칙 준수</li>
     *   <li>불변 Set으로 반환하여 외부에서 권한 목록 변경 방지</li>
     * </ul>
     * 
     * @return 사용자의 권한 목록 (빈 Set 반환 가능, null 반환하지 않음)
     * 
     * @implNote 역할이 null이거나 빈 경우 빈 Set을 반환하여 NPE 방지
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final Set<Role> roles = user.getRoles();
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    /**
     * 사용자의 비밀번호를 반환합니다.
     * 
     * <p><strong>보안 주의사항:</strong>
     * 이 메소드는 Spring Security의 인증 과정에서만 사용되며,
     * 반환된 비밀번호는 이미 해시화된 상태여야 합니다.</p>
     * 
     * @return 해시화된 사용자 비밀번호 (null 가능)
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자명을 반환합니다.
     * 
     * <p>Spring Security에서 사용자를 식별하는 고유한 사용자명으로 사용됩니다.
     * 일반적으로 로그인 시 입력하는 아이디에 해당합니다.</p>
     * 
     * @return 사용자명 (null 불가)
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 계정이 만료되지 않았는지 확인합니다.
     * 
     * <p>현재 구현에서는 계정 만료 기능을 사용하지 않으므로 항상 true를 반환합니다.
     * 향후 계정 만료 기능이 필요한 경우 User 도메인 객체에 만료일 정보를 추가하고
     * 해당 정보를 기반으로 만료 여부를 판단할 수 있습니다.</p>
     * 
     * @return 계정 만료 여부 (true: 만료되지 않음, false: 만료됨)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정이 잠기지 않았는지 확인합니다.
     * 
     * <p>현재 구현에서는 계정 잠금 기능을 사용하지 않으므로 항상 true를 반환합니다.
     * 향후 보안 강화를 위해 로그인 실패 횟수 제한, 계정 잠금 기능을 구현할 경우
     * User 도메인 객체에 잠금 상태 정보를 추가할 수 있습니다.</p>
     * 
     * @return 계정 잠금 여부 (true: 잠기지 않음, false: 잠김)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명(비밀번호)이 만료되지 않았는지 확인합니다.
     * 
     * <p>현재 구현에서는 비밀번호 만료 기능을 사용하지 않으므로 항상 true를 반환합니다.
     * 보안 정책에 따라 정기적인 비밀번호 변경이 필요한 경우
     * User 도메인 객체에 비밀번호 변경일 정보를 추가하여 만료 여부를 판단할 수 있습니다.</p>
     * 
     * @return 자격 증명 만료 여부 (true: 만료되지 않음, false: 만료됨)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정이 활성화되어 있는지 확인합니다.
     * 
     * <p>현재 구현에서는 계정 비활성화 기능을 사용하지 않으므로 항상 true를 반환합니다.
     * 향후 사용자 계정 관리 기능 확장 시 User 도메인 객체에 활성화 상태 정보를 추가하여
     * 관리자가 특정 계정을 비활성화할 수 있는 기능을 구현할 수 있습니다.</p>
     * 
     * @return 계정 활성화 여부 (true: 활성화, false: 비활성화)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}

