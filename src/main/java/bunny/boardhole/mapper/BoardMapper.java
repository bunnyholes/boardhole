package bunny.boardhole.mapper;

import bunny.boardhole.domain.Board;
import bunny.boardhole.dto.board.BoardDto;
import bunny.boardhole.dto.board.BoardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface BoardMapper {
    
    BoardDto toDto(Board board);
    
    @Mapping(source = "author.username", target = "authorName")
    @Mapping(source = "author.id", target = "authorId")
    BoardResponse toResponse(Board board);
    
    BoardResponse dtoToResponse(BoardDto dto);
}