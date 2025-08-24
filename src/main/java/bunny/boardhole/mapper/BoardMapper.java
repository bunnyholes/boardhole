package bunny.boardhole.mapper;

import bunny.boardhole.domain.Board;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BoardMapper {

    @Insert("INSERT INTO boards (title, content, author_id) " +
            "VALUES (#{title}, #{content}, #{authorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Board board);

    @Select("SELECT b.*, u.username AS author_name FROM boards b LEFT JOIN users u ON b.author_id = u.id WHERE b.id = #{id}")
    @Results(id = "boardResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "authorId", column = "author_id"),
            @Result(property = "authorName", column = "author_name"),
            @Result(property = "viewCount", column = "view_count"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Board findById(Long id);

    @Select("SELECT b.*, u.username AS author_name FROM boards b LEFT JOIN users u ON b.author_id = u.id ORDER BY b.created_at DESC")
    @ResultMap("boardResult")
    List<Board> findAll();

    @Update({
            "<script>",
            "UPDATE boards",
            "<set>",
            "  <if test='title != null'>title = #{title},</if>",
            "  <if test='content != null'>content = #{content},</if>",
            "  updated_at = #{updatedAt}",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    void update(Board board);

    @Delete("DELETE FROM boards WHERE id = #{id}")
    void deleteById(Long id);
}
