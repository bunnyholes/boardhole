package bunny.boardhole.admin.application.query;

import bunny.boardhole.admin.presentation.dto.AdminStatsResponse;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 조회 서비스
 * 시스템 통계 및 관리자 전용 조회 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQueryService {

    /** 사용자 정보 조회를 위한 레포지토리 */
    private final UserRepository userRepository;
    
    /** 게시글 정보 조회를 위한 레포지토리 */
    private final BoardRepository boardRepository;
    
    /** 메시지 유틸리티 */
    private final MessageUtils messageUtils;

    /**
     * 시스템 통계 조회
     * 전체 사용자 수, 게시글 수, 전체 조회수 등을 조회합니다.
     *
     * @return 시스템 통계 정보
     */
    public AdminStatsResponse getSystemStats() {
        final long totalUsers = userRepository.count();
        final long totalBoards = boardRepository.count();
        final long totalViews = boardRepository.sumViewCount();
        
        final AdminStatsResponse response = new AdminStatsResponse(totalUsers, totalBoards, totalViews);
        
        if (log.isInfoEnabled()) {
            log.info(messageUtils.getMessage("log.admin.stats.fetched", totalUsers, totalBoards, totalViews));
        }

        return response;
    }
}