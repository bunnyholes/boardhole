package dev.xiyo.bunnyholes.boardhole.user.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dev.xiyo.bunnyholes.boardhole.user.domain.User;

/**
 * 사용자 데이터 접근 리포지토리
 * 사용자 엔티티에 대한 CRUD 작업 및 검색 기능을 제공합니다.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
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
     * 이메일로 사용자 조회
     *
     * @param email 조회할 이메일
     * @return 사용자 엔티티 Optional
     */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(UUID id);

    /**
     * 사용자명, 이름, 이메일로 대소문자 구분 없이 검색
     *
     * @param username 검색할 사용자명 키워드
     * @param name     검색할 이름 키워드
     * @param email    검색할 이메일 키워드
     * @param pageable 페이지네이션 정보
     * @return 검색된 사용자 페이지
     */
    Page<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String name, String email, Pageable pageable);

    /**
     * 삭제된 사용자 포함 전체 조회 (Native Query)
     *
     * @return 삭제 여부 상관없이 모든 사용자 목록
     */
    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllIncludingDeleted();

    /**
     * 삭제된 사용자만 조회 (Native Query)
     *
     * @return 삭제된 사용자 목록
     */
    @Query(value = "SELECT * FROM users WHERE deleted = true", nativeQuery = true)
    List<User> findAllDeleted();

    /**
     * ID로 삭제 여부 상관없이 사용자 조회 (Native Query)
     *
     * @param id 사용자 ID
     * @return 사용자 (삭제 여부 상관없이)
     */
    @Query(value = "SELECT * FROM users WHERE id = ?1", nativeQuery = true)
    Optional<User> findByIdIncludingDeleted(UUID id);
}
