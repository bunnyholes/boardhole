package dev.xiyo.bunnyholes.boardhole.reply.presentation.mapper;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import dev.xiyo.bunnyholes.boardhole.reply.application.command.CreateReplyCommand;
import dev.xiyo.bunnyholes.boardhole.reply.application.command.UpdateReplyCommand;
import dev.xiyo.bunnyholes.boardhole.reply.application.result.ReplyResult;
import dev.xiyo.bunnyholes.boardhole.reply.application.result.ReplyTreeResult;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.dto.CreateReplyRequest;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.dto.ReplyResponse;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.dto.ReplyTreeResponse;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.dto.UpdateReplyRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface ReplyWebMapper {

    @Mapping(target = "boardId", source = "boardId")
    @Mapping(target = "authorUsername", source = "authorUsername")
    CreateReplyCommand toCommand(CreateReplyRequest request, UUID boardId, String authorUsername);

    UpdateReplyCommand toCommand(UpdateReplyRequest request);

    ReplyResponse toResponse(ReplyResult result);

    List<ReplyResponse> toResponseList(List<ReplyResult> results);

    ReplyTreeResponse toResponse(ReplyTreeResult result);
}
