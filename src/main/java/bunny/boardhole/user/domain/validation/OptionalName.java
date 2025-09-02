package bunny.boardhole.user.domain.validation;

import jakarta.validation.*;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 사용자 실명 검증 애너테이션 (선택적 필드)
 * - null 허용
 * - 값이 있을 경우 1-50자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(min = 1, max = 50, message = "{user.validation.name.size}")
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