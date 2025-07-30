package com.redmath.GymManagementApp.member;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/{id}")
    public Optional<Member> getMemberById(@PathVariable Long id) {
        return memberService.getMemberById(id);
    }

    @PostMapping
    public Member createMember(@RequestBody Member member) {
        return memberService.createMember(member);
    }

    @PutMapping("/{id}")
    public Member updateMember(@PathVariable Long id, @RequestBody Member member) {
        return memberService.updateMember(id, member);
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
    }

    @GetMapping("/by-trainer/{trainerId}")
    public List<Member> getMembersByTrainerId(@PathVariable Long trainerId) {
        return memberService.getMembersByTrainerId(trainerId);
    }

    @GetMapping("/me")
    public Member getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        return memberService.getMemberByUsername(username)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }


}
