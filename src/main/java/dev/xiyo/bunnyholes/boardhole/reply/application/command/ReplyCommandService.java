package dev.xiyo.bunnyholes.boardhole.reply.application.command;

import java.util.UUID;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import dev.xiyo.bunnyholes.boardhole.board.domain.Board;
import dev.xiyo.bunnyholes.boardhole.board.infrastructure.BoardRepository;
import dev.xiyo.bunnyholes.boardhole.reply.application.mapper.ReplyMapper;
import dev.xiyo.bunnyholes.boardhole.reply.application.result.ReplyResult;
import dev.xiyo.bunnyholes.boardhole.reply.domain.Reply;
import dev.xiyo.bunnyholes.boardhole.reply.domain.validation.ReplyValidationConstants;
import dev.xiyo.bunnyholes.boardhole.reply.infrastructure.ReplyRepository;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ReplyCommandService {

    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ReplyMapper replyMapper;

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ReplyResult create(@Valid CreateReplyCommand cmd) {
        Board board = boardRepository
            .findById(cmd.boardId())
            .orElseThrow(() -> new ResourceNotFoundException(
                MessageUtils.get("error.board.not-found.id", cmd.boardId())));

        User author = userRepository
            .findByUsername(cmd.authorUsername())
            .orElseThrow(() -> new ResourceNotFoundException(
                MessageUtils.get("error.user.not-found.username", cmd.authorUsername())));

        Reply parent = null;
        if (cmd.parentId() != null) {
            parent = replyRepository
                .findById(cmd.parentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    MessageUtils.get("error.reply.parent-not-found")));

            validateDepth(parent);
            validateBoardMatch(parent, board);
        }

        Reply reply = Reply.builder()
            .board(board)
            .parent(parent)
            .author(author)
            .content(cmd.content())
            .build();

        Reply saved = replyRepository.save(reply);

        log.info(MessageUtils.get("log.reply.created", saved.getId(), board.getId(), author.getUsername()));

        return replyMapper.toResult(saved);
    }

    @Transactional
    @PreAuthorize("hasPermission(#replyId, 'REPLY', 'WRITE')")
    public ReplyResult update(UUID replyId, @Valid UpdateReplyCommand cmd) {
        Reply reply = loadReplyOrThrow(replyId);

        replyMapper.updateReplyFromCommand(cmd, reply);

        Reply saved = replyRepository.save(reply);

        log.info(MessageUtils.get("log.reply.updated", saved.getId(), saved.getAuthor().getUsername()));

        return replyMapper.toResult(saved);
    }

    @Transactional
    @PreAuthorize("hasPermission(#replyId, 'REPLY', 'DELETE')")
    public void delete(UUID replyId) {
        Reply reply = loadReplyOrThrow(replyId);

        boolean hasChildren = !replyRepository.findByParentId(replyId).isEmpty();

        if (hasChildren) {
            reply.markAsDeleted();
            replyRepository.save(reply);
        } else {
            replyRepository.delete(reply);
        }

        log.info(MessageUtils.get("log.reply.deleted", replyId, reply.getAuthor().getUsername()));
    }

    private Reply loadReplyOrThrow(UUID id) {
        return replyRepository
            .findByIdWithAuthor(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                MessageUtils.get("error.reply.not-found.id", id)));
    }

    private void validateDepth(Reply parent) {
        int currentDepth = calculateDepth(parent);
        if (currentDepth >= ReplyValidationConstants.MAX_DEPTH - 1) {
            throw new IllegalArgumentException(
                MessageUtils.get("error.reply.depth-exceeded", ReplyValidationConstants.MAX_DEPTH));
        }
    }

    private int calculateDepth(Reply reply) {
        int depth = 0;
        Reply current = reply;
        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    private void validateBoardMatch(Reply parent, Board board) {
        if (!parent.getBoard().getId().equals(board.getId())) {
            throw new IllegalArgumentException(
                MessageUtils.get("error.reply.board-mismatch"));
        }
    }
}
