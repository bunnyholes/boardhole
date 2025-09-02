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

    /** User repository for database read operations */
    private final UserRepository userRepository;
    
    /** User mapper for entity-result conversions */
    private final UserMapper userMapper;
    
    /** Message utilities for internationalization */
    private final MessageUtils messageUtils;

    /**
     * 사용자 ID로 단일 사용자 조회
     *
     * @param id 조회할 사용자 ID
     * @return 사용자 조회 결과
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public UserResult get(final Long identifier) {
        final UserResult result = userRepository.findById(identifier)
                .map(userMapper::toResult)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", identifier)));
        
        if (log.isDebugEnabled()) {
            log.debug(messageUtils.getMessage("log.user.fetched", identifier));
        }
        
        return result;
    }

    /**
     * 사용자 목록 페이지네이션 조회
     *
     * @param pageable 페이지네이션 정보
     * @return 사용자 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<UserResult> listWithPaging(final Pageable pageable) {
        final Page<UserResult> results = userRepository.findAll(pageable).map(userMapper::toResult);
        
        if (log.isDebugEnabled()) {
            log.debug(messageUtils.getMessage("log.user.list.fetched", results.getTotalElements(), pageable.getPageNumber()));
        }
        
        return results;
    }

    /**
     * 검색어로 사용자 목록 페이지네이션 조회
     *
     * @param pageable 페이지네이션 정보
     * @param search   검색어 (사용자명, 이름, 이메일에서 검색)
     * @return 검색된 사용자 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<UserResult> listWithPaging(final Pageable pageable, final String searchTerm) {
        final Page<UserResult> results = userRepository
                .findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        searchTerm, searchTerm, searchTerm, pageable)
                .map(userMapper::toResult);
        
        if (log.isDebugEnabled()) {
            final String sanitizedSearchTerm = sanitizeForLog(searchTerm);
            log.debug(messageUtils.getMessage("log.user.search.fetched", results.getTotalElements(), sanitizedSearchTerm));
        }
        
        return results;
    }

    /**
     * 로그 출력용 문자열 새니타이징
     * CRLF 인젝션 공격을 방지하기 위해 개행 문자를 제거합니다.
     *
     * @param input 새니타이징할 입력 문자열
     * @return 새니타이징된 문자열
     */
    private String sanitizeForLog(final String input) {
        return (input == null) ? "null" : input.replaceAll("[\r\n]", "_");
    }

}
