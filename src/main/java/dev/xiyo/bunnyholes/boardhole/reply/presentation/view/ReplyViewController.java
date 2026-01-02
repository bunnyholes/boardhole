package dev.xiyo.bunnyholes.boardhole.reply.presentation.view;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import dev.xiyo.bunnyholes.boardhole.reply.application.command.ReplyCommandService;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.dto.CreateReplyRequest;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.dto.UpdateReplyRequest;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.mapper.ReplyWebMapper;

@Controller
@RequiredArgsConstructor
public class ReplyViewController {

    private final ReplyCommandService replyCommandService;
    private final ReplyWebMapper replyWebMapper;

    @PostMapping("/api/boards/{boardId}/replies")
    @PreAuthorize("isAuthenticated()")
    public String create(
        @PathVariable UUID boardId,
        @Validated @ModelAttribute CreateReplyRequest request,
        @AuthenticationPrincipal UserDetails principal
    ) {
        var cmd = replyWebMapper.toCommand(request, boardId, principal.getUsername());
        var result = replyCommandService.create(cmd);
        return "redirect:/boards/" + boardId + "#reply-" + result.id();
    }

    @PostMapping("/replies/{replyId}")
    @PreAuthorize("hasPermission(#replyId, 'REPLY', 'WRITE')")
    public String update(
        @PathVariable UUID replyId,
        @Validated @ModelAttribute UpdateReplyRequest request
    ) {
        var cmd = replyWebMapper.toCommand(request);
        var result = replyCommandService.update(replyId, cmd);
        return "redirect:/boards/" + result.boardId() + "#reply-" + replyId;
    }

    @PostMapping("/replies/{replyId}/delete")
    @PreAuthorize("hasPermission(#replyId, 'REPLY', 'WRITE')")
    public String delete(
        @PathVariable UUID replyId,
        @ModelAttribute("boardId") UUID boardId
    ) {
        replyCommandService.delete(replyId);
        return "redirect:/boards/" + boardId + "#reply-section";
    }
}
