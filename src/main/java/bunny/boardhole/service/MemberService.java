package bunny.boardhole.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MemberService {

    private Map<String, String> members = new HashMap<>();

    public void join(String memberName, String password) {
        members.put(memberName, password);
    }

    public String getMembers() {
        return this.members.toString();
    }
}
