package bunny.boardhole.user.domain.validation.optional;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 사용자 실명 선택적 검증을 위한 커스텀 어노테이션입니다.
 * 
 * <p>다음 조건들을 만족해야 합니다:
 * <ul>
 *   <li>null 값 허용: 선택적 필드로 빈 값 가능</li>
 *   <li>길이 제한: 값이 있을 경우 최소 1자에서 최대 50자까지 허용</li>
 * </ul>
 * 
 * @see ValidationConstants#USER_NAME_MIN_LENGTH
 * @see ValidationConstants#USER_NAME_MAX_LENGTH
 * @since 1.0
 * @author BoardHole Development Team
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(min = ValidationConstants.USER_NAME_MIN_LENGTH, max = ValidationConstants.USER_NAME_MAX_LENGTH, message = "{user.validation.name.size}")
@Constraint(validatedBy = {})
public @interface OptionalName {
    String message() default "{user.validation.name.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OptionalName[] value();
    }
}