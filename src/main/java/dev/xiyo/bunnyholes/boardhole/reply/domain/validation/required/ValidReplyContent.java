package dev.xiyo.bunnyholes.boardhole.reply.domain.validation.required;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import dev.xiyo.bunnyholes.boardhole.reply.domain.validation.ReplyValidationConstants;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{validation.reply.content.required}")
@Size(max = ReplyValidationConstants.CONTENT_MAX_LENGTH, message = "{validation.reply.content.too-long}")
@Constraint(validatedBy = {})
public @interface ValidReplyContent {
    String message() default "{validation.reply.content.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidReplyContent[] value();
    }
}
