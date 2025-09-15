package bunny.boardhole.board.domain.validation.required;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import bunny.boardhole.board.domain.validation.BoardValidationConstants;

/**
 * 게시글 제목 검증 애너테이션
 * - 필수값 검증
 * - 최대 200자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{validation.board.title.required}")
@Size(max = BoardValidationConstants.BOARD_TITLE_MAX_LENGTH, message = "{validation.board.title.too-long}")
@Constraint(validatedBy = {})
public @interface ValidBoardTitle {
    String message() default "{validation.board.title.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidBoardTitle[] value();
    }
}