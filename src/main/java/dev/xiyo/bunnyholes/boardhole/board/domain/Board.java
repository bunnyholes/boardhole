package dev.xiyo.bunnyholes.boardhole.board.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SoftDelete;

import dev.xiyo.bunnyholes.boardhole.board.domain.validation.BoardValidationConstants;
import dev.xiyo.bunnyholes.boardhole.board.domain.validation.required.ValidBoardContent;
import dev.xiyo.bunnyholes.boardhole.board.domain.validation.required.ValidBoardTitle;
import dev.xiyo.bunnyholes.boardhole.shared.domain.BaseEntity;
import dev.xiyo.bunnyholes.boardhole.shared.domain.listener.ValidationListener;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = "author")
@Entity
@EntityListeners(ValidationListener.class)
@DynamicUpdate
@SoftDelete(columnName = "deleted")
@Table(name = "boards", indexes = {@Index(name = "idx_board_title", columnList = "title"), @Index(name = "idx_board_created_at", columnList = "created_at"),})
public class Board extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ValidBoardTitle
    @Column(nullable = false, length = BoardValidationConstants.BOARD_TITLE_MAX_LENGTH)
    private String title;

    @ValidBoardContent
    @Column(nullable = false, length = BoardValidationConstants.BOARD_CONTENT_MAX_LENGTH)
    private String content;

    @NotNull(message = "{validation.board.author.required}")
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private int viewCount = 0;

    @Version
    private Long version;

    // 필요한 필드만 받는 생성자에 @Builder 적용
    @Builder
    public Board(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    // 기존 change 메서드는 하위 호환성을 위해 유지 (deprecated 처리 가능)
    @Deprecated
    public void changeTitle(String title) {
        setTitle(title);
    }

    @Deprecated
    public void changeContent(String content) {
        setContent(content);
    }

    public void increaseViewCount() {
        viewCount++;
    }
}
