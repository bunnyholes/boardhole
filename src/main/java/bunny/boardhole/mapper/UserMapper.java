package bunny.boardhole.mapper;

import bunny.boardhole.domain.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO users (username, password, name, email, created_at, updated_at) " +
            "VALUES (#{username}, #{password}, #{name}, #{email}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM users ORDER BY created_at DESC")
    List<User> findAll();

    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(String username);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);

    @Update({
            "<script>",
            "UPDATE users",
            "<set>",
            "  <if test='name != null'>name = #{name},</if>",
            "  <if test='email != null'>email = #{email},</if>",
            "  <if test='password != null'>password = #{password},</if>",
            "  updated_at = #{updatedAt}",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    void update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(Long id);

    @Update("UPDATE users SET last_login = #{lastLogin} WHERE id = #{id}")
    void updateLastLogin(@Param("id") Long id, @Param("lastLogin") LocalDateTime lastLogin);
}

