package bunny.boardhole.mapper;

import bunny.boardhole.domain.Board;
import bunny.boardhole.dto.common.PageRequest;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {

    @Insert("INSERT INTO boards (title, content, author_id, view_count, created_at, updated_at) " +
            "VALUES (#{title}, #{content}, #{authorId}, #{viewCount}, #{createdAt}, #{updatedAt})")
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
    
    @Select({
        "<script>",
        "SELECT b.*, u.username AS author_name FROM boards b LEFT JOIN users u ON b.author_id = u.id",
        "<where>",
        "  <if test='pageRequest.search != null and pageRequest.search != \"\"'>",
        "    AND (b.title LIKE CONCAT('%', #{pageRequest.search}, '%') OR b.content LIKE CONCAT('%', #{pageRequest.search}, '%'))",
        "  </if>",
        "</where>",
        "ORDER BY",
        "<if test='pageRequest.sortBy == \"title\"'>b.title</if>",
        "<if test='pageRequest.sortBy == \"createdAt\"'>b.created_at</if>",
        "<if test='pageRequest.sortBy == \"viewCount\"'>b.view_count</if>",
        "<if test='pageRequest.sortBy == null or pageRequest.sortBy == \"id\"'>b.id</if>",
        "<if test='pageRequest.sortDirection == \"asc\"'>ASC</if>",
        "<if test='pageRequest.sortDirection == null or pageRequest.sortDirection == \"desc\"'>DESC</if>",
        "LIMIT #{pageRequest.size} OFFSET #{pageRequest.offset}",
        "</script>"
    })
    @ResultMap("boardResult")
    List<Board> findWithPaging(@Param("pageRequest") PageRequest pageRequest);
    
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM boards",
        "<where>",
        "  <if test='search != null and search != \"\"'>",
        "    AND (title LIKE CONCAT('%', #{search}, '%') OR content LIKE CONCAT('%', #{search}, '%'))",
        "  </if>",
        "</where>",
        "</script>"
    })
    long countWithSearch(@Param("search") String search);
}
