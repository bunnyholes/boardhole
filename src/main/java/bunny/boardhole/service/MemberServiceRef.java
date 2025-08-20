package bunny.boardhole.service;

import bunny.boardhole.domain.Member;
import bunny.boardhole.mapper.MemberMapperRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberServiceRef {

    @Autowired
    private MemberMapperRef memberMapperRef;

    /**
     * 회원 가입 (객체 방식)
     * @param memberName 회원명
     * @param password 비밀번호
     */
    public void join(String memberName, String password) {
        // 입력값 검증
        if (memberName == null || memberName.trim().isEmpty()) {
            throw new IllegalArgumentException("회원명은 필수입니다.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (password.length() < 4) {
            throw new IllegalArgumentException("비밀번호는 4자 이상이어야 합니다.");
        }

        // 중복 회원 체크
        Member existingMember = memberMapperRef.selectMemberByName(memberName.trim());
        if (existingMember != null) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다: " + memberName);
        }
        
        Member member = new Member(memberName.trim(), password);
        memberMapperRef.insertMember(member);
    }

    /**
     * 회원 가입 (플랫 파라미터 방식)
     * @param memberName 회원명
     * @param password 비밀번호
     */
    public void joinFlat(String memberName, String password) {
        // 입력값 검증 (동일한 로직)
        if (memberName == null || memberName.trim().isEmpty()) {
            throw new IllegalArgumentException("회원명은 필수입니다.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (password.length() < 4) {
            throw new IllegalArgumentException("비밀번호는 4자 이상이어야 합니다.");
        }

        // 중복 회원 체크
        Member existingMember = memberMapperRef.selectMemberByName(memberName.trim());
        if (existingMember != null) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다: " + memberName);
        }
        
        // 플랫 방식으로 직접 파라미터 전달
        memberMapperRef.insertMemberFlat(memberName.trim(), password);
    }

    /**
     * 전체 회원 조회 (기존 MemberService와 호환성을 위한 메소드)
     * @return 회원 목록 문자열
     */
    public String getMembers() {
        List<Member> members = memberMapperRef.selectAllMembers();
        return members.stream()
                .map(member -> member.getMemberName() + "=" + member.getPassword())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /**
     * 전체 회원 목록 조회
     * @return 회원 목록
     */
    public List<Member> getAllMembers() {
        return memberMapperRef.selectAllMembers();
    }

    /**
     * 회원명으로 회원 조회
     * @param memberName 회원명
     * @return 회원 객체
     */
    public Member getMemberByName(String memberName) {
        return memberMapperRef.selectMemberByName(memberName);
    }

    /**
     * 회원 삭제
     * @param memberName 삭제할 회원명
     */
    public void deleteMember(String memberName) {
        Member member = memberMapperRef.selectMemberByName(memberName);
        if (member != null) {
            memberMapperRef.deleteMemberById(member.getId());
        }
    }

    /**
     * 로그인 검증
     * @param memberName 회원명
     * @param password 비밀번호
     * @return 로그인 성공 여부
     */
    public boolean login(String memberName, String password) {
        Member member = memberMapperRef.selectMemberByName(memberName);
        return member != null && member.getPassword().equals(password);
    }
}