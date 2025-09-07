package bunny.boardhole.user.domain.validation.required;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 이메일 인증 코드 검증을 위한 커스텀 validation 애너테이션
 */
@NotBlank(message = "{validation.email.verification.code.required}")
@Size(min = 6, max = 6, message = "{validation.email.verification.code.size}")
@Pattern(regexp = "^[A-Z0-9]{6}$", message = "{validation.email.verification.code.pattern}")
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVerificationCode {
    String message() default "{validation.email.verification.code.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}