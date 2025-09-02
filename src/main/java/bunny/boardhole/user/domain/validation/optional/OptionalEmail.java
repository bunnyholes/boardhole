package bunny.boardhole.user.domain.validation.optional;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

/**
 * 이메일 검증 애너테이션 (선택적 필드)
 * - null 허용
 * - 값이 있을 경우 이메일 형식 검증
 * - 값이 있을 경우 최대 255자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Email(message = "{user.validation.email.format}")
@Size(max = ValidationConstants.USER_EMAIL_MAX_LENGTH, message = "{user.validation.email.size}")
@Constraint(validatedBy = {})
public @interface OptionalEmail {
    String message() default "{user.validation.email.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OptionalEmail[] value();
    }
}