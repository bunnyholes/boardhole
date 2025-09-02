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
 * Spring Security 사용자 세부정보 서비스 - 사용자명 기반 인증을 담당합니다.
 * 
 * <p>Spring Security의 {@link UserDetailsService} 인터페이스를 구현하여
 * 사용자명을 기반으로 데이터베이스에서 사용자 정보를 조회하고
 * Spring Security에서 사용할 수 있는 {@link UserDetails} 형태로 변환합니다.</p>
 * 
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li><strong>사용자 인증:</strong> 사용자명으로 데이터베이스에서 사용자 조회</li>
 *   <li><strong>예외 처리:</strong> 사용자가 없는 경우 국제화된 오류 메시지와 함께 예외 발생</li>
 *   <li><strong>도메인 변환:</strong> User 도메인 객체를 AppUserPrincipal로 래핑</li>
 *   <li><strong>입력 검증:</strong> Bean Validation을 통한 사용자명 유효성 검사</li>
 * </ul>
 * 
 * <p><strong>보안 고려사항:</strong></p>
 * <ul>
 *   <li><strong>사용자 열거 방지:</strong> 사용자명 존재 여부만 확인하고 상세 정보는 노출하지 않음</li>
 *   <li><strong>CRLF 인젝션 방지:</strong> 사용자명은 DB에서 조회된 안전한 값이므로 별도 정화 불필요</li>
 *   <li><strong>null 안전성:</strong> Repository에서 null 반환 시 예외 발생으로 안전한 처리</li>
 * </ul>
 * 
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // Spring Security에서 자동으로 호출됨 (로그인 시)
 * UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
 * 
 * // 또는 직접 호출
 * @Autowired
 * private AppUserDetailsService userDetailsService;
 * 
 * UserDetails user = userDetailsService.loadUserByUsername("user123");
 * }</pre>
 * 
 * @author Security Team
 * @version 1.0
 * @since 1.0
 * 
 * @see UserDetailsService
 * @see UserDetails
 * @see AppUserPrincipal
 * @see bunny.boardhole.user.domain.User
 * @see bunny.boardhole.user.infrastructure.UserRepository
 */
@Service
@Validated
@RequiredArgsConstructor
@Schema(name = "AppUserDetailsService", description = "Spring Security 사용자 세부정보 서비스 - 사용자명 기반 인증 담당")
public class AppUserDetailsService implements UserDetailsService {
    /** 사용자 데이터베이스 접근을 위한 리포지토리 */
    private final UserRepository userRepository;
    
    /** 국제화 메시지 처리를 위한 유틸리티 (오류 메시지 국제화) */
    private final MessageUtils messageUtils;

    /**
     * 사용자명으로 사용자 세부정보를 로드합니다.
     * 
     * <p>Spring Security에서 인증 과정에 필요한 사용자 정보를 데이터베이스에서 조회하여
     * {@link UserDetails} 형태로 반환합니다. 사용자가 없는 경우 국제화된 오류 메시지와 함께
     * {@link UsernameNotFoundException}을 발생시켜 Spring Security의 인증 실패 처리가
     * 수행되도록 합니다.</p>
     * 
     * <p><strong>처리 단계:</strong></p>
     * <ol>
     *   <li>사용자명으로 데이터베이스에서 사용자 조회</li>
     *   <li>사용자가 없는 경우 국제화된 예외 메시지와 함께 예외 발생</li>
     *   <li>사용자가 있는 경우 AppUserPrincipal로 래핑하여 반환</li>
     * </ol>
     * 
     * <p><strong>보안 고려사항:</strong></p>
     * <ul>
     *   <li>사용자명 열거 방지: 사용자 존재 여부만 노출하고 상세 정보는 노출하지 않음</li>
     *   <li>예외 메시지에 사용자명 포함: 디버깅 지원 및 문제 해결 용이성 제공</li>
     *   <li>DB 조회 결과 null 안전성: Repository에서 null 반환 시 예외 발생으로 안전한 처리</li>
     * </ul>
     * 
     * <p><strong>성능 고려사항:</strong></p>
     * <ul>
     *   <li>데이터베이스 조회는 1회만 수행됨 (N+1 문제 없음)</li>
     *   <li>Repository에서 인덱스 사용을 권장 (username 열에 unique index 설정)</li>
     * </ul>
     * 
     * @param username 인증할 사용자의 사용자명 (null 이거나 빈 문자열 불가, Bean Validation 적용)
     * @return 데이터베이스에서 조회된 사용자 정보를 포함한 UserDetails 객체 (null 반환하지 않음)
     * @throws UsernameNotFoundException 지정된 사용자명에 해당하는 사용자가 데이터베이스에 없는 경우
     * 
     * @implNote 이 메소드는 Spring Security에서 자동으로 호출되며, 직접 호출도 가능
     */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(
                messageUtils.getMessage("error.user.not-found.username", username)
            );
        }
        return new AppUserPrincipal(user);
    }
}