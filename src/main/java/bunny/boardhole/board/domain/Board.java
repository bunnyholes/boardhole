package bunny.boardhole.board.domain;

import bunny.boardhole.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"author"})
@Entity
@Table(name = "boards", indexes = {
        @Index(name = "idx_board_title", columnList = "title"),
        @Index(name = "idx_board_created_at", columnList = "created_at")
})
@Schema(name = "Board", description = "게시글 도메인 엔티티 - 시스템의 핵심 게시글 정보")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Schema(description = "게시글 고유 ID (자동 생성)", example = "1")
    private Long id;

    @Column(nullable = false, length = 200)
    @Schema(description = "게시글 제목 (최대 200자)", example = "안녕하세요, 반갑습니다!")
    private String title;

    @Column(nullable = false, length = 10000)
    @Schema(description = "게시글 내용 (최대 10,000자)", example = "이것은 게시글의 내용입니다.")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @Schema(description = "게시글 작성자 정보")
    private User author;

    @Column(name = "view_count")
    @Schema(description = "조회수 (기본값 0, 자동 증가)", example = "42")
    private Integer viewCount;

    @Version
    @Schema(description = "낭관적 락을 위한 버전 컴럼 (동시성 제어)", example = "1")
    private Long version;

    @Column(name = "created_at")
    @Schema(description = "게시글 작성 일시 (자동 설정)", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "마지막 수정 일시 (자동 갱신)", example = "2024-01-15T15:45:30")
    private LocalDateTime updatedAt;

    // 필요한 필드만 받는 생성자에 @Builder 적용
    @Builder
    public Board(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (viewCount == null) viewCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void increaseViewCount() {
        int current = this.viewCount == null ? 0 : this.viewCount;
        this.viewCount = current + 1;
    }
}
