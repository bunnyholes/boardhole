package bunny.boardhole.common.security;

import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
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
        if (AuthenticationValidator.hasRole(auth, "ROLE_ADMIN")) {
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
        return boardRepository.findById(boardId)
                .map(board -> board.getAuthor() != null ? board.getAuthor().getId() : null)
                .map(ownerId -> isSameUser(auth, ownerId))
                .orElse(false);
    }

    private boolean isSameUser(Authentication auth, Long userId) {
        Long current = extractUserId(auth);
        return current != null && userId != null && current.equals(userId);
    }

    private Long extractUserId(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof AppUserPrincipal p && p.user() != null) {
            return p.user().getId();
        }
        return null;
    }
}

