package bunny.boardhole.shared.security;

import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;
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
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(MessageUtils.get("error.user.not-found.username", username)));
        return new AppUserPrincipal(user);
    }
}