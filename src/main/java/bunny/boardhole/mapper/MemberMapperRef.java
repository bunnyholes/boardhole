package bunny.boardhole.mapper;

import bunny.boardhole.domain.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 회원 데이터 접근을 위한 MyBatis Mapper 인터페이스
 */
@Mapper
public interface MemberMapperRef {

    /**
     * 회원 저장 (객체 방식)
     *
     * @param member 저장할 회원 정보
     * @return 저장된 행의 수
     */
    int insertMember(Member member);

    /**
     * 회원 저장 (플랫 파라미터 방식)
     *
     * @param memberName 회원명
     * @param password   비밀번호
     * @return 저장된 행의 수
     */
    int insertMemberFlat(@Param("memberName") String memberName, @Param("password") String password);

    /**
     * 모든 회원 조회
     *
     * @return 회원 목록
     */
    List<Member> selectAllMembers();

    /**
     * ID로 회원 조회
     *
     * @param id 회원 ID
     * @return 회원 정보
     */
    Member selectMemberById(@Param("id") Long id);

    /**
     * 회원명으로 회원 조회
     *
     * @param memberName 회원명
     * @return 회원 정보
     */
    Member selectMemberByName(@Param("memberName") String memberName);

    /**
     * 회원 정보 수정
     *
     * @param member 수정할 회원 정보
     * @return 수정된 행의 수
     */
    int updateMember(Member member);

    /**
     * 회원 삭제
     *
     * @param id 삭제할 회원 ID
     * @return 삭제된 행의 수
     */
    int deleteMemberById(@Param("id") Long id);

    /**
     * 회원 수 조회
     *
     * @return 전체 회원 수
     */
    int countMembers();

    @Select("""
            SELECT member_name, password FROM MEMBERS;
""")
    String selectMembers();
}