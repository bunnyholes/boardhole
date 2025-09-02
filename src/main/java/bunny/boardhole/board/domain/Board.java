package bunny.boardhole.board.domain;

import bunny.boardhole.shared.constants.ValidationConstants;
import bunny.boardhole.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"author"})
@Entity
@DynamicUpdate
@Table(name = "boards", indexes = {
        @Index(name = "idx_board_title", columnList = "title"),
        @Index(name = "idx_board_created_at", columnList = "created_at")
})
/**
 * 게시글 도메인 엔티티 클래스.
 * <p>
 * 시스템의 핵심 게시글 정보를 관리하는 엔티티입니다.
 * JPA를 통해 데이터베이스와 매핑되며, 게시글의 생성, 수정, 조회수 관리 등의
 * 핵심 비즈니스 로직을 캡슐화합니다.
 * </p>
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>게시글 기본 정보 관리 (제목, 내용, 작성자)</li>
 *   <li>조회수 자동 증가</li>
 *   <li>생성/수정 시각 자동 관리</li>
 *   <li>낙관적 락을 통한 동시성 제어</li>
 * </ul>
 *
 * @author Board Team
 * @version 1.0
 * @since 1.0
 */
@Schema(name = "Board", description = "게시글 도메인 엔티티 - 시스템의 핵심 게시글 정보")
public class Board {

    // 에러 메시지 상수
    private static final String TITLE_REQUIRED_MESSAGE = "게시글 제목은 필수입니다";
    private static final String TITLE_MAX_LENGTH_MESSAGE = "게시글 제목은 " + ValidationConstants.BOARD_TITLE_MAX_LENGTH + "자를 초과할 수 없습니다";
    private static final String CONTENT_REQUIRED_MESSAGE = "게시글 내용은 필수입니다";
    private static final String CONTENT_MAX_LENGTH_MESSAGE = "게시글 내용은 " + ValidationConstants.BOARD_CONTENT_MAX_LENGTH + "자를 초과할 수 없습니다";
    private static final String AUTHOR_REQUIRED_MESSAGE = "작성자는 필수입니다";
    /** 게시글 고유 식별자 (자동 생성) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Schema(description = "게시글 고유 ID (자동 생성)", example = "1")
    private Long id;

    /** 게시글 제목 */
    @Column(nullable = false, length = ValidationConstants.BOARD_TITLE_MAX_LENGTH)
    @Schema(description = "게시글 제목 (최대 200자)", example = "안녕하세요, 반갑습니다!")
    private String title;

    /** 게시글 내용 */
    @Column(nullable = false, length = ValidationConstants.BOARD_CONTENT_MAX_LENGTH)
    @Schema(description = "게시글 내용 (최대 10,000자)", example = "이것은 게시글의 내용입니다.")
    private String content;

    /** 게시글 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @Schema(description = "게시글 작성자 정보")
    private User author;

    /** 조회수 (기본값 0, 자동 증가) */
    @Column(name = "view_count")
    @Schema(description = "조회수 (기본값 0, 자동 증가)", example = "42")
    private Integer viewCount;

    /** 낙관적 락 버전 */
    @Version
    @Schema(description = "낙관적 락을 위한 버전 컬럼 (동시성 제어)", example = "1")
    private Long version;

    /** 생성 일시 */
    @Column(name = "created_at")
    @Schema(description = "게시글 작성 일시 (자동 설정)", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    /** 수정 일시 */
    @Column(name = "updated_at")
    @Schema(description = "마지막 수정 일시 (자동 갱신)", example = "2024-01-15T15:45:30")
    private LocalDateTime updatedAt;

    /**
     * 게시글을 생성하는 Builder 패턴 생성자입니다.
     * <p>
     * 필수 필드인 제목, 내용, 작성자를 받아 새로운 게시글 객체를 생성합니다.
     * 생성 시 유효성 검증을 수행하여 도메인 규칙을 강제합니다.
     * </p>
     *
     * @param title   게시글 제목 (필수, 최대 200자)
     * @param content 게시글 내용 (필수, 최대 10,000자)
     * @param author  게시글 작성자 (필수)
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    @Builder
    public Board(@NonNull final String title, @NonNull final String content, @NonNull final User author) {
        Assert.hasText(title, TITLE_REQUIRED_MESSAGE);
        Assert.isTrue(title.length() <= ValidationConstants.BOARD_TITLE_MAX_LENGTH, TITLE_MAX_LENGTH_MESSAGE);
        Assert.hasText(content, CONTENT_REQUIRED_MESSAGE);
        Assert.isTrue(content.length() <= ValidationConstants.BOARD_CONTENT_MAX_LENGTH, CONTENT_MAX_LENGTH_MESSAGE);
        Assert.notNull(author, AUTHOR_REQUIRED_MESSAGE);

        this.title = title;
        this.content = content;
        this.author = author;
    }

    /**
     * JPA 엔티티 영속화 전 실행되는 콜백 메소드입니다.
     * <p>
     * 생성일시, 수정일시, 조회수를 초기화합니다.
     * 데이터베이스에 저장되기 전 필수 필드들의 기본값을 설정합니다.
     * </p>
     */
    @PrePersist
    public void prePersist() {
        final LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (viewCount == null) {
            viewCount = 0;
        }
    }

    /**
     * JPA 엔티티 업데이트 전 실행되는 콜백 메소드입니다.
     * <p>
     * 수정일시를 현재 시각으로 자동 갱신합니다.
     * 엔티티가 변경될 때마다 자동으로 실행됩니다.
     * </p>
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 게시글 제목을 변경합니다.
     * <p>
     * 제목 변경 시 유효성 검증을 수행하여 도메인 규칙을 준수합니다.
     * 빈 문자열이나 최대 길이를 초과하는 제목은 허용되지 않습니다.
     * </p>
     *
     * @param title 새로운 게시글 제목 (필수, 최대 200자)
     * @throws IllegalArgumentException 제목이 유효하지 않은 경우
     */
    public void changeTitle(@NonNull final String title) {
        Assert.hasText(title, TITLE_REQUIRED_MESSAGE);
        Assert.isTrue(title.length() <= ValidationConstants.BOARD_TITLE_MAX_LENGTH, TITLE_MAX_LENGTH_MESSAGE);
        this.title = title;
    }

    /**
     * 게시글 내용을 변경합니다.
     * <p>
     * 내용 변경 시 유효성 검증을 수행하여 도메인 규칙을 준수합니다.
     * 빈 문자열이나 최대 길이를 초과하는 내용은 허용되지 않습니다.
     * </p>
     *
     * @param content 새로운 게시글 내용 (필수, 최대 10,000자)
     * @throws IllegalArgumentException 내용이 유효하지 않은 경우
     */
    public void changeContent(@NonNull final String content) {
        Assert.hasText(content, CONTENT_REQUIRED_MESSAGE);
        Assert.isTrue(content.length() <= ValidationConstants.BOARD_CONTENT_MAX_LENGTH, CONTENT_MAX_LENGTH_MESSAGE);
        this.content = content;
    }

    /**
     * 게시글의 조회수를 1 증가시킵니다.
     * <p>
     * 게시글이 조회될 때마다 호출되어 조회수를 자동으로 관리합니다.
     * null-safe 처리를 통해 초기값이 null인 경우에도 안전하게 처리됩니다.
     * </p>
     */
    public void increaseViewCount() {
        final int current = this.viewCount == null ? 0 : this.viewCount;
        this.viewCount = current + 1;
    }
}
