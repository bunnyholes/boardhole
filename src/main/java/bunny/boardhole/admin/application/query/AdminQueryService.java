package bunny.boardhole.admin.application.query;

import bunny.boardhole.admin.presentation.dto.AdminStatsResponse;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 조회 서비스
 * 시스템 통계 및 관리자 전용 조회 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQueryService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    /**
     * 시스템 통계 조회
     * 전체 사용자 수, 게시글 수, 전체 조회수 등을 조회합니다.
     *
     * @return 시스템 통계 정보
     */
    public AdminStatsResponse getSystemStats() {
        long totalUsers = userRepository.count();
        long totalBoards = boardRepository.count();
        long totalViews = boardRepository.sumViewCount();

        return new AdminStatsResponse(totalUsers, totalBoards, totalViews);
    }
}