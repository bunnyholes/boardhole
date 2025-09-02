package bunny.boardhole.shared.security;

import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.*;

/**
 * 현재 인증된 사용자 정보를 컨트롤러 메소드 파라미터로 주입하는 Spring MVC ArgumentResolver입니다.
 * 
 * <p>Spring MVC의 {@link HandlerMethodArgumentResolver} 인터페이스를 구현하여
 * {@link CurrentUser} 어노테이션이 적용된 {@link User} 타입의 메소드 파라미터에
 * 현재 인증된 사용자 도메인 객체를 자동으로 주입합니다.</p>
 * 
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li><strong>사용자 주입:</strong> {@code @CurrentUser User user} 파라미터에 현재 인증된 사용자 객체 주입</li>
 *   <li><strong>인증 검증:</strong> 인증되지 않은 사용자의 경우 AccessDeniedException 발생</li>
 *   <li><strong>타입 안전성:</strong> AppUserPrincipal에서 안전하게 User 도메인 객체 추출</li>
 *   <li><strong>오류 처리:</strong> 국제화된 오류 메시지와 함께 의미 있는 예외 발생</li>
 * </ul>
 * 
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * @RestController
 * public class UserController {
 *     
 *     @GetMapping("/profile")
 *     public UserProfile getProfile(@CurrentUser User user) {
 *         // 현재 인증된 사용자 정보 사용
 *         return userService.getProfile(user.getId());
 *     }
 *     
 *     @PutMapping("/profile")
 *     public void updateProfile(@CurrentUser User user, @RequestBody ProfileDto dto) {
 *         // 현재 사용자 ID로 프로필 업데이트
 *         userService.updateProfile(user.getId(), dto);
 *     }
 * }
 * }</pre>
 * 
 * <p><strong>보안 고려사항:</strong></p>
 * <ul>
 *   <li><strong>인증 검증:</strong> 인증되지 않은 요청에 대해 즉시 AccessDeniedException 발생</li>
 *   <li><strong>Principal 타입 검증:</strong> AppUserPrincipal 타입이 아닌 경우 예외 발생</li>
 *   <li><strong>국제화 오류:</strong> 사용자에게 의미 있는 오류 메시지 제공</li>
 *   <li><strong>null 안전성:</strong> 모든 null 상황에 대한 예외 처리</li>
 * </ul>
 * 
 * @author Security Team
 * @version 1.0
 * @since 1.0
 * 
 * @see HandlerMethodArgumentResolver
 * @see CurrentUser
 * @see AppUserPrincipal
 * @see bunny.boardhole.user.domain.User
 * @see org.springframework.security.access.AccessDeniedException
 */
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    /** 국제화 오류 메시지 처리를 위한 유틸리티 */
    private final MessageUtils messageUtils;

    /**
     * 메소드 파라미터가 이 Resolver에서 처리 가능한지 확인합니다.
     * 
     * <p>{@link CurrentUser} 어노테이션이 적용되고 파라미터 타입이 {@link User}인 경우를
     * 지원합니다. 다른 타입의 파라미터나 어노테이션이 없는 경우는 지원하지 않습니다.</p>
     * 
     * <p><strong>지원 조건:</strong></p>
     * <ul>
     *   <li>{@code @CurrentUser} 어노테이션이 있어야 함</li>
     *   <li>파라미터 타입이 {@code User.class}와 정확히 일치해야 함</li>
 * </ul>
     * 
     * @param parameter 검사할 메소드 파라미터 (null 안전)
     * @return 이 Resolver에서 처리 가능한 경우 true, 그렇지 않으면 false
     * 
     * @implNote Spring MVC에서 자동으로 호출되는 메소드로 직접 호출하지 않음
     */
    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(User.class);
    }

    /**
     * 메소드 파라미터에 주입할 현재 인증된 사용자 객체를 생성합니다.
     * 
     * <p>Spring Security의 SecurityContextHolder에서 현재 인증 정보를 가져와
     * AppUserPrincipal에서 사용자 도메인 객체를 추출하여 반환합니다.
     * 인증되지 않은 사용자이거나 잘못된 Principal 타입인 경우 예외를 발생시킵니다.</p>
     * 
     * <p><strong>처리 단계:</strong></p>
     * <ol>
     *   <li>SecurityContextHolder에서 현재 인증 정보 획득</li>
     *   <li>인증 상태 검증 (비인증 상태 시 예외 발생)</li>
     *   <li>Principal 타입 검증 (AppUserPrincipal 아닌 경우 예외 발생)</li>
     *   <li>사용자 도메인 객체 추출 및 반환</li>
     * </ol>
     * 
     * <p><strong>보안 검증:</strong></p>
     * <ul>
     *   <li>인증 객체가 null이아닌지 검증</li>
     *   <li>인증 상태가 유효한지 검증</li>
     *   <li>Principal이 예상 타입(AppUserPrincipal)인지 검증</li>
     *   <li>User 도메인 객체가 null이 아닌지 검증</li>
     * </ul>
     * 
     * @param parameter 대상 메소드 파라미터 (사용되지 않음)
     * @param mavContainer ModelAndViewContainer (사용되지 않음)
     * @param webRequest 현재 웹 요청 (사용되지 않음)
     * @param binderFactory 데이터 바인딩 팩토리 (사용되지 않음)
     * @return 현재 인증된 사용자의 User 도메인 객체 (null 반환하지 않음)
     * 
     * @throws AccessDeniedException 인증되지 않은 사용자이거나 잘못된 Principal 타입인 경우
     * 
     * @implNote Spring MVC에서 자동으로 호출되는 메소드로 직접 호출하지 않음
     */
    @Override
    public Object resolveArgument(final MethodParameter parameter,
                                  final ModelAndViewContainer mavContainer,
                                  final NativeWebRequest webRequest,
                                  final WebDataBinderFactory binderFactory) {

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException(messageUtils.getMessage("error.auth.required"));
        }

        if (!(auth.getPrincipal() instanceof AppUserPrincipal(User user))) {
            throw new AccessDeniedException(messageUtils.getMessage("error.auth.invalid-principal"));
        }

        return user;
    }
}