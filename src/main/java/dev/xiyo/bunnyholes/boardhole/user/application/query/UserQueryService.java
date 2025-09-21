package dev.xiyo.bunnyholes.boardhole.user.application.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.application.mapper.UserMapper;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

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

    /**
     * 사용자 ID로 단일 사용자 조회
     *
     * @param id 조회할 사용자 ID
     * @return 사용자 조회 결과
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or #username.equalsIgnoreCase(authentication.name)")
    public UserResult get(String username) {
        return userRepository
                .findByUsername(username)
                .map(userMapper::toResult)
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.user.not-found.username", username)));
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
                .findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, search, pageable)
                .map(userMapper::toResult);
    }

    // WebController 호환 메서드들 (기존 API 유지)

    /**
     * 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<UserResult> getUsers(Pageable pageable) {
        return listWithPaging(pageable);
    }

    /**
     * 사용자 단일 조회 (권한 체크 없는 버전)
     */
    @Transactional(readOnly = true)
    public UserResult getUser(String username) {
        return userRepository.findByUsername(username)
                             .map(userMapper::toResult)
                             .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.user.not-found.username", username)));
    }

    // 대시보드용 메서드들

    /**
     * 활성 사용자 수 조회 (간단한 구현: 전체 사용자 수)
     * TODO: 실제로는 최근 로그인 기록 등을 기반으로 활성 사용자를 판단해야 함
     *
     * @return 활성 사용자 수
     */
    @Transactional(readOnly = true)
    public Long getActiveUserCount() {
        return userRepository.count();
    }

    /**
     * 이름 중복 체크
     *
     * @param name 확인할 이름
     * @param excludeUsername 제외할 사용자명 (자기 자신 제외)
     * @return 중복되면 true, 사용 가능하면 false
     */
    @Transactional(readOnly = true)
    public boolean isNameDuplicated(String name, String excludeUsername) {
        // 현재 사용자와 같은 이름인지 확인
        User currentUser = userRepository.findByUsername(excludeUsername).orElse(null);
        if (currentUser != null && currentUser.getName().equals(name)) {
            return false; // 자기 자신의 현재 이름과 같으면 중복이 아님
        }
        
        // 다른 사용자가 이 이름을 사용하는지 확인
        return userRepository.existsByName(name);
    }

}
