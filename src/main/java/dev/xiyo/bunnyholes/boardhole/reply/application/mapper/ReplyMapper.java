package dev.xiyo.bunnyholes.boardhole.reply.application.mapper;

import java.util.ArrayList;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import dev.xiyo.bunnyholes.boardhole.reply.application.command.UpdateReplyCommand;
import dev.xiyo.bunnyholes.boardhole.reply.application.result.ReplyResult;
import dev.xiyo.bunnyholes.boardhole.reply.domain.Reply;
import dev.xiyo.bunnyholes.boardhole.reply.infrastructure.ReplyTreeProjection;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = ArrayList.class)
@SuppressWarnings("NullableProblems")
public interface ReplyMapper {

    @Mapping(target = "boardId", source = "board.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.username")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "depth", constant = "0")
    @Mapping(target = "children", expression = "java(new ArrayList<>())")
    ReplyResult toResult(Reply reply);

    @Mapping(target = "boardId", ignore = true)
    @Mapping(target = "children", expression = "java(new ArrayList<>())")
    ReplyResult toResult(ReplyTreeProjection projection);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReplyFromCommand(UpdateReplyCommand command, @MappingTarget Reply reply);
}
