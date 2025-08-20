package bunny.boardhole.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {

    @Insert("""
            INSERT INTO MEMBERS(member_name, password) VALUES (#{membername}, #{password})
            """)
    void insertMember(String memberName, String password);

}
