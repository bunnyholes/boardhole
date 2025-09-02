package bunny.boardhole.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 권한 열거형 클래스.
 * 시스템의 접근 권한을 정의하는 열거형입니다.
 * 사용자의 역할에 따라 시스템의 기능에 대한 접근 범위를 결정하며,
 * Spring Security와 통합되어 인가 및 인증 시스템에서 활용됩니다.
 *
 * @author User Team
 * @version 1.0
 * @since 1.0
 */
@Schema(name = "Role", description = "사용자 권한 열거형 - 시스템 접근 권한을 정의")
public enum Role {
    /**
     * 관리자 권한.
     * <p>
     * 시스템의 모든 기능에 접근할 수 있는 최고 급 권한입니다.
     * 사용자 관리, 시스템 설정, 보안 관리 등 전범위한 관리 업무를 수행할 수 있습니다.
     * </p>
     */
    @Schema(description = "관리자 권한 - 모든 기능에 접근 가능")
    ADMIN,
    
    /**
     * 일반 사용자 권한.
     * <p>
     * 시스템의 기본 기능에 접근할 수 있는 일반적인 사용자 권한입니다.
     * 게시글 작성, 수정, 조회 등 기본적인 기능을 사용할 수 있습니다.
     * </p>
     */
    @Schema(description = "일반 사용자 권한 - 기본 기능에 접근 가능")
    USER
}

