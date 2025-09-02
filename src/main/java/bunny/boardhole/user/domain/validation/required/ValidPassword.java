package bunny.boardhole.user.domain.validation.required;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

/**
 * 사용자 비밀번호 필수 검증을 위한 커스텀 어노테이션입니다.
 * 
 * <p>다음 조건들을 만족해야 합니다:
 * <ul>
 *   <li>필수값 검증: null, 빈 문자열, 공백 문자만 포함된 문자열 허용하지 않음</li>
 *   <li>길이 제한: 최소 8자에서 최대 100자까지 허용</li>
 *   <li>복잡도 요구사항: 영문 대문자, 소문자, 숫자, 특수문자 모두 최소 1개씩 포함</li>
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
@NotBlank(message = "{user.validation.password.required}")
@Size(min = ValidationConstants.USER_PASSWORD_MIN_LENGTH, max = ValidationConstants.USER_PASSWORD_MAX_LENGTH, message = "{user.validation.password.size}")
@Pattern(
        regexp = ValidationConstants.PASSWORD_PATTERN,
        message = "{user.validation.password.pattern}"
)
@Constraint(validatedBy = {})
public @interface ValidPassword {
    String message() default "{user.validation.password.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidPassword[] value();
    }
}