package bunny.boardhole.board.domain;

import bunny.boardhole.shared.constants.*;
import bunny.boardhole.shared.util.EntityMessageProvider;
import bunny.boardhole.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.*;

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
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = ValidationConstants.BOARD_TITLE_MAX_LENGTH)
    private String title;

    @Column(nullable = false, length = ValidationConstants.BOARD_CONTENT_MAX_LENGTH)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "view_count")
    private Integer viewCount;

    @Version
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 필요한 필드만 받는 생성자에 @Builder 적용
    @Builder
    public Board(@NonNull String title, @NonNull String content, @NonNull User author) {
        Assert.hasText(title, EntityMessageProvider.getMessage(ValidationMessages.BOARD_TITLE_REQUIRED, ValidationMessages.BOARD_TITLE_REQUIRED_FALLBACK));
        Assert.isTrue(title.length() <= ValidationConstants.BOARD_TITLE_MAX_LENGTH, EntityMessageProvider.getMessage(ValidationMessages.BOARD_TITLE_TOO_LONG, ValidationMessages.BOARD_TITLE_TOO_LONG_FALLBACK, ValidationConstants.BOARD_TITLE_MAX_LENGTH));
        Assert.hasText(content, EntityMessageProvider.getMessage(ValidationMessages.BOARD_CONTENT_REQUIRED, ValidationMessages.BOARD_CONTENT_REQUIRED_FALLBACK));
        Assert.isTrue(content.length() <= ValidationConstants.BOARD_CONTENT_MAX_LENGTH, EntityMessageProvider.getMessage(ValidationMessages.BOARD_CONTENT_TOO_LONG, ValidationMessages.BOARD_CONTENT_TOO_LONG_FALLBACK, ValidationConstants.BOARD_CONTENT_MAX_LENGTH));
        Assert.notNull(author, EntityMessageProvider.getMessage(ValidationMessages.BOARD_AUTHOR_REQUIRED, ValidationMessages.BOARD_AUTHOR_REQUIRED_FALLBACK));

        this.title = title;
        this.content = content;
        this.author = author;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (viewCount == null) viewCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void changeTitle(@NonNull String title) {
        Assert.hasText(title, EntityMessageProvider.getMessage(ValidationMessages.BOARD_TITLE_REQUIRED, ValidationMessages.BOARD_TITLE_REQUIRED_FALLBACK));
        Assert.isTrue(title.length() <= ValidationConstants.BOARD_TITLE_MAX_LENGTH, EntityMessageProvider.getMessage(ValidationMessages.BOARD_TITLE_TOO_LONG, ValidationMessages.BOARD_TITLE_TOO_LONG_FALLBACK, ValidationConstants.BOARD_TITLE_MAX_LENGTH));
        this.title = title;
    }

    public void changeContent(@NonNull String content) {
        Assert.hasText(content, EntityMessageProvider.getMessage(ValidationMessages.BOARD_CONTENT_REQUIRED, ValidationMessages.BOARD_CONTENT_REQUIRED_FALLBACK));
        Assert.isTrue(content.length() <= ValidationConstants.BOARD_CONTENT_MAX_LENGTH, EntityMessageProvider.getMessage(ValidationMessages.BOARD_CONTENT_TOO_LONG, ValidationMessages.BOARD_CONTENT_TOO_LONG_FALLBACK, ValidationConstants.BOARD_CONTENT_MAX_LENGTH));
        this.content = content;
    }

    public void increaseViewCount() {
        int current = viewCount == null ? 0 : viewCount;
        viewCount = current + 1;
    }
}
