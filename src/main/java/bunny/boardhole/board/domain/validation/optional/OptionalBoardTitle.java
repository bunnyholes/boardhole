package bunny.boardhole.board.domain.validation.optional;

import java.lang.annotation.*;

import bunny.boardhole.shared.constants.ValidationConstants;

import jakarta.validation.*;
import jakarta.validation.constraints.Size;

/**
 * 게시글 제목 검증 애너테이션 (선택적 필드)
 * - null 허용
 * - 값이 있을 경우 최대 200자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(max = ValidationConstants.BOARD_TITLE_MAX_LENGTH, message = "{validation.board.title.size}")
@Constraint(validatedBy = {})
public @interface OptionalBoardTitle {
    String message() default "{validation.board.title.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OptionalBoardTitle[] value();
    }
}