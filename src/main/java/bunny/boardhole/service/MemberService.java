package bunny.boardhole.service;

import bunny.boardhole.mapper.MemberMapperRef;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MemberService {

    private final MemberMapperRef memberMapperRef;

    public MemberService(MemberMapperRef memberMapperRef) {
        this.memberMapperRef = memberMapperRef;
    }

    public void join(String memberName, String password) {
        this.memberMapperRef.insertMemberFlat(memberName, password);
    }

    public String getMembers() {
        return this.memberMapperRef.selectMembers();
    }
}
