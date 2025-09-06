package bunny.boardhole.board.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.Assert;

import bunny.boardhole.shared.constants.ValidationConstants;
import bunny.boardhole.shared.domain.BaseEntity;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = "author")
@Entity
@DynamicUpdate
@Table(name = "boards", indexes = {@Index(name = "idx_board_title", columnList = "title"), @Index(name = "idx_board_created_at", columnList = "created_at")})
public class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = ValidationConstants.BOARD_TITLE_MAX_LENGTH)
    private String title;

    @Column(nullable = false, length = ValidationConstants.BOARD_CONTENT_MAX_LENGTH)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_board_author"))
    private User author;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Version
    private Long version;

    // 필요한 필드만 받는 생성자에 @Builder 적용
    @Builder
    public Board(String title, String content, User author) {
        Assert.hasText(title, MessageUtils.get("validation.board.title.required"));
        Assert.isTrue(title.length() <= ValidationConstants.BOARD_TITLE_MAX_LENGTH, MessageUtils.get("validation.board.title.too-long", ValidationConstants.BOARD_TITLE_MAX_LENGTH));
        Assert.hasText(content, MessageUtils.get("validation.board.content.required"));
        Assert.isTrue(content.length() <= ValidationConstants.BOARD_CONTENT_MAX_LENGTH, MessageUtils.get("validation.board.content.too-long", ValidationConstants.BOARD_CONTENT_MAX_LENGTH));
        Assert.notNull(author, MessageUtils.get("validation.board.author.required"));

        this.title = title;
        this.content = content;
        this.author = author;
    }

    public void changeTitle(String title) {
        Assert.hasText(title, MessageUtils.get("validation.board.title.required"));
        Assert.isTrue(title.length() <= ValidationConstants.BOARD_TITLE_MAX_LENGTH, MessageUtils.get("validation.board.title.too-long", ValidationConstants.BOARD_TITLE_MAX_LENGTH));
        this.title = title;
    }

    public void changeContent(String content) {
        Assert.hasText(content, MessageUtils.get("validation.board.content.required"));
        Assert.isTrue(content.length() <= ValidationConstants.BOARD_CONTENT_MAX_LENGTH, MessageUtils.get("validation.board.content.too-long", ValidationConstants.BOARD_CONTENT_MAX_LENGTH));
        this.content = content;
    }

    public void increaseViewCount() {
        viewCount++;
    }
}
