package bunny.boardhole.user.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 비밀번호 검증 애너테이션 (선택적 필드)
 * - null 허용
 * - 값이 있을 경우 8-100자 제한
 * - 값이 있을 경우 영문 대소문자, 숫자, 특수문자 중 3가지 이상 포함
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(min = 8, max = 100, message = "{user.validation.password.size}")
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
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