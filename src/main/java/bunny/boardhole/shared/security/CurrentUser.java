package bunny.boardhole.shared.security;

import java.lang.annotation.*;

/**
 * 현재 인증된 사용자 정보를 컨트롤러 메소드 파라미터에 주입하기 위한 어노테이션입니다.
 * 
 * <p>Spring MVC의 {@link org.springframework.web.method.support.HandlerMethodArgumentResolver}와
 * 함께 사용되어 {@link User} 타입의 메소드 파라미터에 현재 인증된 사용자의 도메인 객체를
 * 자동으로 주입합니다. 이를 통해 컨트롤러에서 SecurityContextHolder를 직접 사용할 필요가 없어집니다.</p>
 * 
 * <p><strong>사용법:</strong></p>
 * <pre>{@code
 * @RestController
 * public class UserController {
 *     
 *     @GetMapping("/profile")
 *     public UserProfile getMyProfile(@CurrentUser User user) {
 *         // 현재 인증된 사용자의 정보 사용
 *         return userService.getProfile(user.getId());
 *     }
 *     
 *     @PutMapping("/profile")
 *     public void updateMyProfile(@CurrentUser User user, @RequestBody ProfileDto dto) {
 *         // 현재 사용자만 자신의 프로필을 수정할 수 있음
 *         userService.updateProfile(user.getId(), dto);
 *     }
 * }
 * }</pre>
 * 
 * <p><strong>보안 고려사항:</strong></p>
 * <ul>
 *   <li><strong>인증 필수:</strong> 인증되지 않은 사용자의 경우 AccessDeniedException 발생</li>
 *   <li><strong>타입 안전성:</strong> User 타입의 파라미터에만 사용 가능</li>
 *   <li><strong>자동 검증:</strong> Principal 타입 검증을 통한 안전한 사용자 정보 추출</li>
 *   <li><strong>세션 기반:</strong> 현재 HTTP 세션의 인증 정보를 기반으로 동작</li>
 * </ul>
 * 
 * <p><strong>제약사항:</strong></p>
 * <ul>
 *   <li>메소드 파라미터에만 적용 가능 ({@link ElementType#PARAMETER})</li>
 *   <li>User 타입의 파라미터에만 동작 (다른 타입은 무시)</li>
 *   <li>인증이 필요한 엔드포인트에서만 사용 권장</li>
 * </ul>
 * 
 * <p><strong>대안적 접근법:</strong></p>
 * <pre>{@code
 * // 이 어노테이션 없이 직접 사용하는 방법 (권장하지 않음)
 * Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 * AppUserPrincipal principal = (AppUserPrincipal) auth.getPrincipal();
 * User user = principal.user();
 * 
 * // @CurrentUser 어노테이션 사용 (권장)
 * public void someMethod(@CurrentUser User user) {
 *     // 간단하고 안전한 사용자 정보 접근
 * }
 * }</pre>
 * 
 * @author Security Team
 * @version 1.0
 * @since 1.0
 * 
 * @see CurrentUserArgumentResolver
 * @see AppUserPrincipal
 * @see bunny.boardhole.user.domain.User
 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}