package bunny.boardhole.common.security;

import bunny.boardhole.user.domain.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import bunny.boardhole.common.util.MessageUtils;

import java.util.Optional;

public final class AuthenticationValidator {
    
    private AuthenticationValidator() {}
    
    public static User requireAuthenticatedUser(Authentication auth) {
        Assert.notNull(auth, "Authentication must not be null");
        if (!auth.isAuthenticated()) {
            throw new AccessDeniedException(MessageUtils.getMessageStatic("error.auth.not-authenticated"));
        }
        
        return Optional.of(auth.getPrincipal())
                .filter(AppUserPrincipal.class::isInstance)
                .map(AppUserPrincipal.class::cast)
                .map(AppUserPrincipal::user)
                .orElseThrow(() -> new AccessDeniedException(MessageUtils.getMessageStatic("error.auth.invalid-principal")));
    }
    
    public static boolean hasRole(Authentication auth, String role) {
        return Optional.ofNullable(auth)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getAuthorities)
                .orElse(java.util.Set.of())
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
    
    public static boolean isOwnerOrAdmin(Long resourceOwnerId, Authentication auth) {
        if (hasRole(auth, "ROLE_ADMIN")) {
            return true;
        }
        
        return Optional.ofNullable(resourceOwnerId)
                .filter(id -> id > 0)
                .map(id -> requireAuthenticatedUser(auth).getId().equals(id))
                .orElse(false);
    }
}