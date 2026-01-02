package dev.xiyo.bunnyholes.boardhole.shared.security;

import java.io.Serializable;
import java.util.Locale;

import lombok.RequiredArgsConstructor;

import org.springframework.lang.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import dev.xiyo.bunnyholes.boardhole.board.infrastructure.BoardRepository;
import dev.xiyo.bunnyholes.boardhole.reply.infrastructure.ReplyRepository;
import dev.xiyo.bunnyholes.boardhole.shared.constants.PermissionType;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

@Component
@RequiredArgsConstructor
public class AppPermissionEvaluator implements PermissionEvaluator {

    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;

    private static boolean hasRole(Authentication auth, String role) {
        if (!auth.isAuthenticated())
            return false;
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }

    private static boolean isSameUser(Authentication auth, String username) {
        String current = extractUsername(auth);
        return current != null && current.equalsIgnoreCase(username);
    }

    private static @Nullable String extractUsername(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return null;
        return auth.getName();
    }

    private boolean isEmailVerified(Authentication auth) {
        String username = extractUsername(auth);
        if (username == null)
            return false;
        return userRepository.findByUsername(username)
                             .map(dev.xiyo.bunnyholes.boardhole.user.domain.User::isEmailVerified)
                             .orElse(false);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // Not used in this project; rely on id + type form
        return false;
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if (!auth.isAuthenticated())
            return false;
        String type = targetType.toUpperCase(Locale.ROOT);
        String perm = permission.toString().toUpperCase(Locale.ROOT);

        // Admin shortcut
        if (hasRole(auth, "ROLE_ADMIN"))
            return true;

        String targetIdentifier = targetId instanceof String string ? string : String.valueOf(targetId);

        return switch (type) {
            case PermissionType.TARGET_BOARD -> switch (perm) {
                case PermissionType.WRITE, PermissionType.DELETE -> isBoardOwner(auth, targetIdentifier);
                default -> false;
            };
            case PermissionType.TARGET_REPLY -> switch (perm) {
                case PermissionType.WRITE, PermissionType.DELETE -> isReplyOwner(auth, targetIdentifier);
                default -> false;
            };
            case PermissionType.TARGET_USER -> switch (perm) {
                case PermissionType.READ, PermissionType.WRITE, PermissionType.DELETE -> isSameUser(auth, targetIdentifier);
                default -> false;
            };
            case PermissionType.TARGET_EMAIL_VERIFICATION -> switch (perm) {
                case "VERIFIED" -> isEmailVerified(auth);
                default -> false;
            };
            default -> false;
        };
    }

    private boolean isBoardOwner(Authentication auth, String boardId) {
        try {
            return boardRepository.findAuthorUsernameById(java.util.UUID.fromString(boardId))
                                  .map(ownerUsername -> isSameUser(auth, ownerUsername))
                                  .orElse(false);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isReplyOwner(Authentication auth, String replyId) {
        try {
            return replyRepository.findByIdWithAuthor(java.util.UUID.fromString(replyId))
                                  .map(reply -> isSameUser(auth, reply.getAuthor().getUsername()))
                                  .orElse(false);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
