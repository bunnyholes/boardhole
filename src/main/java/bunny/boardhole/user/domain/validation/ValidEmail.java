package bunny.boardhole.user.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 이메일 검증 애너테이션
 * - 필수값 검증
 * - 이메일 형식 검증
 * - 최대 255자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{user.validation.email.required}")
@Email(message = "{user.validation.email.format}")
@Size(max = 255, message = "{user.validation.email.size}")
@Constraint(validatedBy = {})
public @interface ValidEmail {
    String message() default "{user.validation.email.invalid}";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidEmail[] value();
    }
}