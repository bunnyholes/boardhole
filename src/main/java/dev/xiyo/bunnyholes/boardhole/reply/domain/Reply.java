package dev.xiyo.bunnyholes.boardhole.reply.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.DynamicUpdate;
import org.jspecify.annotations.Nullable;

import dev.xiyo.bunnyholes.boardhole.board.domain.Board;
import dev.xiyo.bunnyholes.boardhole.reply.domain.validation.ReplyValidationConstants;
import dev.xiyo.bunnyholes.boardhole.reply.domain.validation.required.ValidReplyContent;
import dev.xiyo.bunnyholes.boardhole.shared.domain.BaseEntity;
import dev.xiyo.bunnyholes.boardhole.shared.domain.listener.ValidationListener;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"board", "parent", "author"})
@Entity
@EntityListeners(ValidationListener.class)
@DynamicUpdate
@Table(name = "replies", indexes = {
    @Index(name = "idx_reply_board_id", columnList = "board_id"),
    @Index(name = "idx_reply_parent_id", columnList = "parent_id"),
    @Index(name = "idx_reply_created_at", columnList = "created_at")
})
public class Reply extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull(message = "{validation.reply.board.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Reply parent;

    @NotNull(message = "{validation.reply.author.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Setter
    @ValidReplyContent
    @Column(nullable = false, length = ReplyValidationConstants.CONTENT_MAX_LENGTH)
    private String content;

    @Column(nullable = false)
    private boolean deleted = false;

    @Builder
    protected Reply(Board board, @Nullable Reply parent, User author, String content) {
        this.board = board;
        this.parent = parent;
        this.author = author;
        this.content = content;
    }

    public void markAsDeleted() {
        this.deleted = true;
    }

    public boolean isRoot() {
        return parent == null;
    }
}
