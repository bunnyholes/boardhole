package bunny.boardhole.user.infrastructure;

import java.util.Optional;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import bunny.boardhole.user.domain.User;

/**
 * 사용자 데이터 접근 리포지토리
 * 사용자 엔티티에 대한 CRUD 작업 및 검색 기능을 제공합니다.
 */
@Repository
@Validated
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 사용자명 중복 확인
     *
     * @param username 확인할 사용자명
     * @return 사용자명 존재 여부
     */
    boolean existsByUsername(String username);

    /**
     * 이메일 중복 확인
     *
     * @param email 확인할 이메일 주소
     * @return 이메일 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 사용자명으로 사용자 조회 (권한 정보 포함)
     *
     * @param username 조회할 사용자명
     * @return 사용자 엔티티 (권한 정보 포함)
     */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

    /**
     * 사용자명으로 사용자 조회 (Optional)
     *
     * @param username 조회할 사용자명
     * @return 사용자 엔티티 Optional
     */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findOptionalByUsername(String username);

    /**
     * 이메일로 사용자 조회
     *
     * @param email 조회할 이메일
     * @return 사용자 엔티티 Optional
     */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(Long id);

    /**
     * 사용자명, 이름, 이메일로 대소문자 구분 없이 검색
     *
     * @param username 검색할 사용자명 키워드
     * @param name     검색할 이름 키워드
     * @param email    검색할 이메일 키워드
     * @param pageable 페이지네이션 정보
     * @return 검색된 사용자 페이지
     */
    Page<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String name, String email, Pageable pageable);
}
