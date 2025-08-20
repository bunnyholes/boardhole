package bunny.boardhole.controller;

import bunny.boardhole.service.MemberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/join")
    public void join(String memberName, String password) {
        this.memberService.join(memberName, password);
    }

    @GetMapping("/members")
    public String getMembers() {
        return this.memberService.getMembers();
    }
}
