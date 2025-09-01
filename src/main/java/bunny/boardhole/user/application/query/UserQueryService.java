package bunny.boardhole.user.application.query;

import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.application.mapper.UserMapper;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 조회 서비스
 * CQRS 패턴의 Query 측면으로 사용자 조회 전용 비지니스 로직을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MessageUtils messageUtils;

    /**
     * 사용자 ID로 단일 사용자 조회
     *
     * @param id 조회할 사용자 ID
     * @return 사용자 조회 결과
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public UserResult get(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResult)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", id)));
    }

    /**
     * 사용자 목록 페이지네이션 조회
     *
     * @param pageable 페이지네이션 정보
     * @return 사용자 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<UserResult> listWithPaging(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResult);
    }

    /**
     * 검색어로 사용자 목록 페이지네이션 조회
     *
     * @param pageable 페이지네이션 정보
     * @param search   검색어 (사용자명, 이름, 이메일에서 검색)
     * @return 검색된 사용자 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<UserResult> listWithPaging(Pageable pageable, String search) {
        return userRepository
                .findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        search, search, search, pageable)
                .map(userMapper::toResult);
    }


}
