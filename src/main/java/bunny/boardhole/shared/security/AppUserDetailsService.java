package bunny.boardhole.shared.security;

import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 사용자 인증 세부 정보 서비스
 * Spring Security UserDetailsService 구현체로 사용자명 기반 인증을 담당합니다.
 */
@Service
@Validated
@RequiredArgsConstructor
@Schema(name = "AppUserDetailsService", description = "Spring Security 사용자 세부정보 서비스 - 사용자명 기반 인증 담당")
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final MessageUtils messageUtils;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException(messageUtils.getMessage("error.user.not-found.username", username));
        return new AppUserPrincipal(user);
    }
}