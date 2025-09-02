package bunny.boardhole.shared.security;

import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.config.properties.SecurityProperties;
import bunny.boardhole.shared.constants.PermissionType;
import bunny.boardhole.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.*;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Locale;

/**
 * 애플리케이션의 사용자 권한 평가를 수행하는 컴포넌트입니다.
 * Spring Security의 PermissionEvaluator 인터페이스를 구현하여 리소스 기반 접근 제어를 제공합니다.
 * 게시글 작성자 및 사용자 본인 권한 확인을 지원합니다.
 */
@Component
@RequiredArgsConstructor
public class AppPermissionEvaluator implements PermissionEvaluator {

    /** 게시글 데이터베이스 접근을 위한 리포지토리 */
    private final BoardRepository boardRepository;
    
    /** 사용자 데이터베이스 접근을 위한 리포지토리 */
    private final UserRepository userRepository;
    
    /** 보안 관련 설정 정보 */
    private final SecurityProperties securityProperties;

    /**
     * 도메인 객체 기반 권한 확인 메소드입니다.
     * 현재 프로젝트에서는 사용하지 않으며, ID + 타입 방식을 사용합니다.
     * 
     * @param authentication 인증 정보
     * @param targetDomainObject 대상 도메인 객체
     * @param permission 권한 정보
     * @return 항상 false (사용하지 않음)
     */
    @Override
    public boolean hasPermission(final Authentication authentication, final Object targetDomainObject, final Object permission) {
        // Not used in this project; rely on id + type form
        return false;
    }

    /**
     * ID와 타입 기반 권한 확인 메소드입니다.
     * 지정된 리소스 ID와 타입에 대해 사용자가 지정된 권한을 가지고 있는지 확인합니다.
     * 
     * @param authentication 인증 정보
     * @param targetIdentifier 대상 리소스 ID
     * @param targetType 대상 리소스 타입 (BOARD, USER 등)
     * @param permission 필요한 권한 (READ, WRITE, DELETE 등)
     * @return 권한 유무 (true: 규한 있음, false: 규한 없음)
     */
    @Override
    public boolean hasPermission(final Authentication authentication, final Serializable targetIdentifier, final String targetType, final Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        final String type = targetType == null ? "" : targetType.toUpperCase(Locale.ROOT);
        final String permissionString = (permission == null ? "" : permission.toString().toUpperCase(Locale.ROOT));

        // Admin shortcut - 관리자는 모든 규한 지원
        if (hasRole(authentication, securityProperties.getRolePrefix() + "ADMIN")) {
            return true;
        }

        if (!(targetIdentifier instanceof Long identifier)) {
            return false;
        }

        return switch (type) {
            case PermissionType.TARGET_BOARD -> switch (permissionString) {
                case PermissionType.WRITE, PermissionType.DELETE -> isBoardOwner(authentication, identifier);
                default -> false;
            };
            case PermissionType.TARGET_USER -> switch (permissionString) {
                case PermissionType.WRITE, PermissionType.DELETE -> isSameUser(authentication, identifier);
                default -> false;
            };
            default -> false;
        };
    }

    /**
     * 지정된 게시글의 소유자인지 확인하는 메소드입니다.
     * 성능 최적화를 위해 작성자 ID만 조회하는 경량 쿼리를 사용합니다.
     * 
     * @param authentication 인증 정보
     * @param boardIdentifier 게시글 ID
     * @return 소유자 여부 (true: 소유자, false: 비소유자)
     * 
     * @implNote TODO: 성능 최적화 - 실제 부하 발생 시 캐싱 고려
     *           현재는 매 권한 체크마다 DB 조회가 발생하지만,
     *           실제 운영 환경에서 부하가 발생하면 다음과 같은 최적화 가능:
     *           1. @Cacheable 적용으로 권한 체크 결과 캐싱
     *           2. Spring Security의 MethodSecurityExpressionOperations 캐싱 활용
     */
    private boolean isBoardOwner(final Authentication authentication, final Long boardIdentifier) {
        // N+1 문제 해결: 작성자 ID만 조회하는 경량 쿼리 사용
        // 전체 Board 엔티티를 로드하지 않고 필요한 정보만 조회하여 성능 최적화
        return boardRepository.findAuthorIdById(boardIdentifier)
                .map(ownerId -> isSameUser(authentication, ownerId))
                .orElse(false);
    }

    /**
     * 인증된 사용자가 지정된 사용자 ID와 동일한지 확인하는 메소드입니다.
     * 
     * @param authentication 인증 정보
     * @param userIdentifier 비교할 사용자 ID
     * @return 동일 사용자 여부 (true: 동일, false: 다름)
     */
    private boolean isSameUser(final Authentication authentication, final Long userIdentifier) {
        final Long currentUserId = extractUserId(authentication);
        return currentUserId != null && currentUserId.equals(userIdentifier);
    }

    /**
     * Authentication 객체에서 사용자 ID를 추출하는 메소드입니다.
     * AppUserPrincipal에서 User 도메인 객체를 가져와 ID를 반환합니다.
     * 
     * @param authentication 인증 정보
     * @return 사용자 ID (null 가능)
     */
    private Long extractUserId(final Authentication authentication) {
        final Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserPrincipal(bunny.boardhole.user.domain.User user) && user != null) {
            return user.getId();
        }
        return null;
    }

    /**
     * 인증된 사용자가 지정된 역할을 가지고 있는지 확인하는 메소드입니다.
     * 
     * @param authentication 인증 정보
     * @param role 확인할 역할 이름
     * @return 역할 소유 여부 (true: 소유, false: 비소유)
     */
    private boolean hasRole(final Authentication authentication, final String role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}

