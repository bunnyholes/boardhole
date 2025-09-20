package dev.xiyo.bunnyholes.boardhole.board.domain.validation.required;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import dev.xiyo.bunnyholes.boardhole.board.domain.validation.BoardValidationConstants;

/**
 * 게시글 내용 검증 애너테이션
 * - 필수값 검증
 * - 최대 10,000자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{validation.board.content.required}")
@Size(max = BoardValidationConstants.BOARD_CONTENT_MAX_LENGTH, message = "{validation.board.content.too-long}")
@Constraint(validatedBy = {})
public @interface ValidBoardContent {
    String message() default "{validation.board.content.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidBoardContent[] value();
    }
}