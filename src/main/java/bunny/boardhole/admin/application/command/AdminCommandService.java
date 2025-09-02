package bunny.boardhole.admin.application.command;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * 관리자 명령 서비스
 * CQRS 패턴의 Command 측면으로 관리자 전용 명령 작업을 담당합니다.
 * Facade 패턴을 적용하여 다른 도메인을 조작할 수 있습니다.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommandService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final MessageUtils messageUtils;

    /**
     * 사용자 차단/차단해제
     *
     * @param cmd 사용자 관리 명령
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public void manageUser(@Valid @NonNull ManageUserCommand cmd) {
        User user = userRepository.findById(cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", cmd.userId())));

        switch (cmd.action()) {
            case BLOCK -> {
                // 관리자는 차단할 수 없음
                if (user.getRoles().contains(Role.ADMIN)) {
                    throw new IllegalArgumentException(messageUtils.getMessage("error.admin.cannot-block-admin"));
                }
                // 실제로는 User 도메인에 blocked 필드가 있어야 하지만, 현재는 로그만 남김
                log.info(messageUtils.getMessage("log.admin.user.blocked", user.getUsername()));
            }
            case UNBLOCK -> {
                log.info(messageUtils.getMessage("log.admin.user.unblocked", user.getUsername()));
            }
            case GRANT_ADMIN -> {
                if (!user.getRoles().contains(Role.ADMIN)) {
                    user.getRoles().add(Role.ADMIN);
                    userRepository.save(user);
                    log.info(messageUtils.getMessage("log.admin.user.granted-admin", user.getUsername()));
                }
            }
            case REVOKE_ADMIN -> {
                if (user.getRoles().contains(Role.ADMIN) && user.getRoles().size() > 1) {
                    user.getRoles().remove(Role.ADMIN);
                    userRepository.save(user);
                    log.info(messageUtils.getMessage("log.admin.user.revoked-admin", user.getUsername()));
                }
            }
        }
    }

    /**
     * 콘텐츠 관리 (게시글 삭제/숨김)
     *
     * @param cmd 콘텐츠 관리 명령
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    public void manageContent(@Valid @NonNull ManageContentCommand cmd) {
        Board board = boardRepository.findById(cmd.boardId())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.board.not-found.id", cmd.boardId())));

        switch (cmd.action()) {
            case DELETE -> {
                boardRepository.delete(board);
                log.info(messageUtils.getMessage("log.admin.content.deleted", board.getId(), board.getTitle()));
            }
            case HIDE -> {
                // 실제로는 Board 도메인에 hidden 필드가 있어야 하지만, 현재는 로그만 남김
                log.info(messageUtils.getMessage("log.admin.content.hidden", board.getId(), board.getTitle()));
            }
            case SHOW -> {
                log.info(messageUtils.getMessage("log.admin.content.shown", board.getId(), board.getTitle()));
            }
        }
    }

    /**
     * 시스템 설정 업데이트
     *
     * @param cmd 시스템 설정 업데이트 명령
     */
    @Transactional
    public void updateSystemSettings(@Valid @NonNull UpdateSystemSettingsCommand cmd) {
        // 실제 구현에서는 시스템 설정을 저장하는 별도의 엔티티가 있어야 함
        // 현재는 로그만 남김
        log.info(messageUtils.getMessage("log.admin.system.settings-updated", cmd.settingKey(), cmd.settingValue()));
    }
}