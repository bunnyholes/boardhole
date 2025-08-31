package bunny.boardhole.common.security;

import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class AppPermissionEvaluator implements PermissionEvaluator {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // Not used in this project; rely on id + type form
        return false;
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if (auth == null || !auth.isAuthenticated()) return false;
        String type = targetType == null ? "" : targetType.toUpperCase();
        String perm = (permission == null ? "" : permission.toString().toUpperCase());

        // Admin shortcut
        if (hasRole(auth, "ROLE_ADMIN")) {
            return true;
        }

        if (!(targetId instanceof Long id)) return false;

        return switch (type) {
            case "BOARD" -> switch (perm) {
                case "WRITE", "DELETE" -> isBoardOwner(auth, id);
                default -> false;
            };
            case "USER" -> switch (perm) {
                case "WRITE", "DELETE" -> isSameUser(auth, id);
                default -> false;
            };
            default -> false;
        };
    }

    private boolean isBoardOwner(Authentication auth, Long boardId) {
        // TODO: 성능 최적화 - 실제 부하 발생 시 캐싱 고려
        // 현재는 매 권한 체크마다 DB 조회가 발생하지만,
        // 실제 운영 환경에서 부하가 발생하면 다음과 같은 최적화 가능:
        // 1. @Cacheable 적용으로 권한 체크 결과 캐싱
        // 2. Spring Security의 MethodSecurityExpressionOperations 캐싱 활용

        // N+1 문제 해결: 작성자 ID만 조회하는 경량 쿼리 사용
        // 전체 Board 엔티티를 로드하지 않고 필요한 정보만 조회하여 성능 최적화
        return boardRepository.findAuthorIdById(boardId)
                .map(ownerId -> isSameUser(auth, ownerId))
                .orElse(false);
    }

    private boolean isSameUser(Authentication auth, Long userId) {
        Long current = extractUserId(auth);
        return current != null && current.equals(userId);
    }

    private Long extractUserId(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof AppUserPrincipal(bunny.boardhole.user.domain.User user) && user != null) {
            return user.getId();
        }
        return null;
    }

    private boolean hasRole(Authentication auth, String role) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}

