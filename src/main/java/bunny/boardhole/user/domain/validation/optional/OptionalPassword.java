package bunny.boardhole.user.domain.validation.optional;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

/**
 * 사용자 비밀번호 선택적 검증을 위한 커스텀 어노테이션입니다.
 * 
 * <p>다음 조건들을 만족해야 합니다:
 * <ul>
 *   <li>null 값 허용: 선택적 필드로 빈 값 가능</li>
 *   <li>길이 제한: 값이 있을 경우 최소 8자에서 최대 100자까지 허용</li>
 *   <li>복잡도 요구사항: 값이 있을 경우 영문 대문자, 소문자, 숫자, 특수문자 모두 최소 1개씩 포함</li>
 * </ul>
 * 
 * @see ValidationConstants#USER_PASSWORD_MIN_LENGTH
 * @see ValidationConstants#USER_PASSWORD_MAX_LENGTH
 * @see ValidationConstants#PASSWORD_PATTERN
 * @since 1.0
 * @author BoardHole Development Team
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(min = ValidationConstants.USER_PASSWORD_MIN_LENGTH, max = ValidationConstants.USER_PASSWORD_MAX_LENGTH, message = "{user.validation.password.size}")
@Pattern(
        regexp = ValidationConstants.PASSWORD_PATTERN,
        message = "{user.validation.password.pattern}"
)
@Constraint(validatedBy = {})
public @interface OptionalPassword {
    String message() default "{user.validation.password.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OptionalPassword[] value();
    }
}