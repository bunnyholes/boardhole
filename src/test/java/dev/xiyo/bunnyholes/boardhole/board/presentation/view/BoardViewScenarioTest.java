package dev.xiyo.bunnyholes.boardhole.board.presentation.view;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * TODO: MockMvc 기반으로 게시판 관련 뷰 시나리오를 다시 구현해야 한다.
 * <p>
 * 기존 Playwright 테스트에서 검증하던 흐름을 유지하기 위해 비활성화된 테스트를 남겨둔다.
 */
@Tag("view")
class BoardViewScenarioTest {

    @Test
    @Disabled("TODO: /boards 목록 화면 접근 흐름을 MockMvc 통합 테스트로 복원한다")
    @DisplayName("게시판 목록 - 인증 상태에 따른 접근 흐름")
    void boardListAccessFlow() {
        // 이전 Playwright BoardViewE2ETest에서 검증한 내용:
        // 1. 비로그인 사용자가 /boards에 접근하면 로그인 페이지로 리다이렉션된다.
        // 2. 로그인 성공 후 RequestCache에 저장된 URL로 복귀한다.
        // 3. 게시글 목록이 최신순으로 정렬되어 렌더링된다.
    }

    @Test
    @Disabled("TODO: /boards/{id} 조회 화면을 MockMvc 통합 테스트로 복원한다")
    @DisplayName("게시판 상세 - 권한 및 렌더링")
    void boardDetailRendering() {
        // 이전 Playwright BoardViewE2ETest에서 검증한 내용:
        // 1. 존재하지 않는 게시글 접근 시 404 페이지가 노출된다.
        // 2. 작성자와 관리자 권한에 따라 수정/삭제 버튼 노출이 다르다.
        // 3. 댓글 작성 폼이 인증 사용자에게만 보인다.
    }

    @Test
    @Disabled("TODO: 게시글 작성 화면과 제출 흐름을 MockMvc 통합 테스트로 복원한다")
    @DisplayName("게시글 작성 - 폼 렌더링과 검증")
    void boardWriteFlow() {
        // 이전 Playwright BoardWriteViewE2ETest에서 검증한 내용:
        // 1. /boards/write 접근 시 CSRF 토큰이 포함된 폼이 노출된다.
        // 2. 필수 필드 누락 시 검증 오류 메시지가 나타난다.
        // 3. 유효한 데이터 제출 후 목록 페이지로 리다이렉션되며 성공 메시지가 표시된다.
    }

    @Test
    @Disabled("TODO: 게시글 수정 흐름을 MockMvc 통합 테스트로 복원한다")
    @DisplayName("게시글 수정 - 권한과 상태 유지")
    void boardEditFlow() {
        // 이전 Playwright BoardEditViewE2ETest에서 검증한 내용:
        // 1. 작성자만 /boards/{id}/edit 페이지에 접근할 수 있다.
        // 2. 기존 게시글 내용이 폼에 프리필된다.
        // 3. 수정 성공 시 상세 페이지로 리다이렉션되며 변경 사항이 반영된다.
    }
}
